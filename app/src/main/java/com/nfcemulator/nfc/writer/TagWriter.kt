package com.nfcemulator.nfc.writer

import android.nfc.Tag
import android.nfc.tech.MifareClassic
import android.nfc.tech.NfcA
import com.nfcemulator.dump.model.TagDump
import com.nfcemulator.dump.model.TagType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

sealed class WriteProgress {
    data object Idle : WriteProgress()
    data object WaitingForTag : WriteProgress()
    data class Writing(val sector: Int, val total: Int) : WriteProgress()
    data class Complete(val sectorsWritten: Int, val totalSectors: Int) : WriteProgress()
    data class Error(val message: String) : WriteProgress()
}

class TagWriter {

    private val _progress = MutableStateFlow<WriteProgress>(WriteProgress.Idle)
    val progress: StateFlow<WriteProgress> = _progress.asStateFlow()

    fun startWaiting() {
        _progress.value = WriteProgress.WaitingForTag
    }

    fun reset() {
        _progress.value = WriteProgress.Idle
    }

    suspend fun writeTag(tag: Tag, dump: TagDump): Boolean {
        return withContext(Dispatchers.IO) {
            when (dump.type) {
                TagType.MIFARE_CLASSIC_1K, TagType.MIFARE_CLASSIC_4K -> writeMifareClassic(tag, dump)
                else -> {
                    _progress.value = WriteProgress.Error("Writing only supported for Mifare Classic tags")
                    false
                }
            }
        }
    }

    private fun writeMifareClassic(tag: Tag, dump: TagDump): Boolean {
        val mfc = MifareClassic.get(tag)
        if (mfc == null) {
            _progress.value = WriteProgress.Error("Target tag is not Mifare Classic")
            return false
        }

        var sectorsWritten = 0
        val totalSectors = dump.sectors.size

        try {
            mfc.connect()

            // Step 1: Try to write UID (block 0) — only works on "magic" Gen1a/Gen2 tags
            val block0Data = dump.sectors.firstOrNull()?.blocks?.firstOrNull()?.data
            if (block0Data != null && block0Data.size == 16) {
                val uidWritten = writeBlock0Magic(tag, mfc, block0Data)
                if (!uidWritten) {
                    _progress.value = WriteProgress.Error(
                        "Cannot write UID — this is not a magic tag (Gen1a/Gen2). " +
                        "Use a UID-modifiable tag."
                    )
                    return false
                }
            }

            // Step 2: Write all sectors
            for (sector in dump.sectors) {
                _progress.value = WriteProgress.Writing(sector.index, totalSectors)

                // Authenticate with known keys
                val defaultKey = byteArrayOf(
                    0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(),
                    0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte()
                )

                var authenticated = false

                // Try key A from dump
                val keyA = sector.keyA
                if (keyA != null) {
                    try {
                        if (mfc.authenticateSectorWithKeyA(sector.index, keyA)) {
                            authenticated = true
                        }
                    } catch (_: Exception) {}
                }

                // Try key B from dump
                if (!authenticated) {
                    val keyB = sector.keyB
                    if (keyB != null) {
                        try {
                            if (mfc.authenticateSectorWithKeyB(sector.index, keyB)) {
                                authenticated = true
                            }
                        } catch (_: Exception) {}
                    }
                }

                // Try default key
                if (!authenticated) {
                    try {
                        if (mfc.authenticateSectorWithKeyA(sector.index, defaultKey)) {
                            authenticated = true
                        }
                    } catch (_: Exception) {}
                }

                if (!authenticated) {
                    try {
                        if (mfc.authenticateSectorWithKeyB(sector.index, defaultKey)) {
                            authenticated = true
                        }
                    } catch (_: Exception) {}
                }

                if (!authenticated) {
                    // Skip this sector but continue with others
                    continue
                }

                // Write blocks in this sector
                val firstBlock = mfc.sectorToBlock(sector.index)
                for (block in sector.blocks) {
                    // Skip block 0 of sector 0 (already written via magic command)
                    if (block.index == 0) continue

                    try {
                        mfc.writeBlock(firstBlock + (block.index - sector.blocks.first().index), block.data)
                    } catch (e: Exception) {
                        // Trailer blocks may fail to write on non-magic tags — continue
                        if (!block.isTrailerBlock) {
                            _progress.value = WriteProgress.Error(
                                "Failed to write block ${block.index}: ${e.message}"
                            )
                            return false
                        }
                    }
                }

                sectorsWritten++
            }

            _progress.value = WriteProgress.Complete(sectorsWritten, totalSectors)
            return sectorsWritten > 0
        } catch (e: Exception) {
            _progress.value = WriteProgress.Error("Write failed: ${e.message}")
            return false
        } finally {
            try { mfc.close() } catch (_: Exception) {}
        }
    }

    private fun writeBlock0Magic(tag: Tag, mfc: MifareClassic, block0: ByteArray): Boolean {
        // Method 1: Direct write (Gen2 / CUID magic tags)
        try {
            val defaultKey = byteArrayOf(
                0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(),
                0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte()
            )
            if (mfc.authenticateSectorWithKeyA(0, defaultKey)) {
                mfc.writeBlock(0, block0)
                return true
            }
        } catch (_: Exception) {}

        // Method 2: Gen1a magic backdoor via NfcA raw commands
        try {
            mfc.close()
            val nfcA = NfcA.get(tag) ?: return false
            nfcA.connect()
            nfcA.timeout = 1000

            // Gen1a unlock: send HALT then magic wakeup bytes
            try {
                // Magic wakeup sequence for Gen1a
                val halt = byteArrayOf(0x50.toByte(), 0x00.toByte())
                try { nfcA.transceive(halt) } catch (_: Exception) {}

                // Chinese magic backdoor command
                val unlock1 = byteArrayOf(0x40.toByte())
                val response1 = nfcA.transceive(unlock1)

                if (response1.isNotEmpty()) {
                    val unlock2 = byteArrayOf(0x43.toByte())
                    nfcA.transceive(unlock2)

                    // Write block 0
                    val writeCmd = ByteArray(2)
                    writeCmd[0] = 0xA0.toByte() // WRITE command
                    writeCmd[1] = 0x00.toByte() // Block 0
                    nfcA.transceive(writeCmd)
                    nfcA.transceive(block0)

                    nfcA.close()
                    mfc.connect() // Reconnect MifareClassic for remaining writes
                    return true
                }
            } catch (_: Exception) {}

            nfcA.close()
            mfc.connect()
        } catch (_: Exception) {
            try { mfc.connect() } catch (_: Exception) {}
        }

        return false
    }
}
