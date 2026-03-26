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

    var pendingDump: TagDump? = null
        private set

    fun startWaiting(dump: TagDump) {
        pendingDump = dump
        _progress.value = WriteProgress.WaitingForTag
    }

    fun startWaiting() {
        _progress.value = WriteProgress.WaitingForTag
    }

    fun reset() {
        pendingDump = null
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

        val defaultKey = byteArrayOf(
            0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(),
            0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte()
        )

        var sectorsWritten = 0
        val totalSectors = dump.sectors.size

        if (totalSectors == 0) {
            _progress.value = WriteProgress.Error("No sectors to write — dump is empty")
            return false
        }

        try {
            mfc.connect()
            mfc.setTimeout(5000)

            // Step 1: Try to write block 0 (UID) — optional, don't stop if it fails
            val block0Data = dump.sectors.firstOrNull()?.blocks?.firstOrNull()?.data
            if (block0Data != null && block0Data.size == 16) {
                val uidWritten = writeBlock0Magic(tag, mfc, block0Data, defaultKey)
                if (!uidWritten) {
                    // Not a magic tag for UID — continue writing data sectors anyway
                    ensureConnected(mfc)
                }
            }

            // Step 2: Write all sectors
            for (sector in dump.sectors) {
                _progress.value = WriteProgress.Writing(sector.index, totalSectors)

                // On a blank CUID card, all keys are FFFFFFFFFFFF
                // Try default key first (blank card), then dump keys (if card was already written)
                var authenticated = false

                // Try default key A (blank CUID card)
                try {
                    ensureConnected(mfc)
                    if (mfc.authenticateSectorWithKeyA(sector.index, defaultKey)) {
                        authenticated = true
                    }
                } catch (_: Exception) {
                    reconnect(mfc)
                }

                // Try default key B
                if (!authenticated) {
                    try {
                        ensureConnected(mfc)
                        if (mfc.authenticateSectorWithKeyB(sector.index, defaultKey)) {
                            authenticated = true
                        }
                    } catch (_: Exception) {
                        reconnect(mfc)
                    }
                }

                // Try dump key A
                if (!authenticated) {
                    val keyA = sector.keyA
                    if (keyA != null) {
                        try {
                            ensureConnected(mfc)
                            if (mfc.authenticateSectorWithKeyA(sector.index, keyA)) {
                                authenticated = true
                            }
                        } catch (_: Exception) {
                            reconnect(mfc)
                        }
                    }
                }

                // Try dump key B
                if (!authenticated) {
                    val keyB = sector.keyB
                    if (keyB != null) {
                        try {
                            ensureConnected(mfc)
                            if (mfc.authenticateSectorWithKeyB(sector.index, keyB)) {
                                authenticated = true
                            }
                        } catch (_: Exception) {
                            reconnect(mfc)
                        }
                    }
                }

                if (!authenticated) {
                    continue
                }

                // Write blocks in this sector
                val firstBlock = mfc.sectorToBlock(sector.index)
                var blocksWritten = 0

                for ((blockIdx, block) in sector.blocks.withIndex()) {
                    // Skip block 0 (UID) — already handled
                    if (sector.index == 0 && blockIdx == 0) continue

                    val absoluteBlock = firstBlock + blockIdx

                    try {
                        // Re-authenticate before each write
                        ensureConnected(mfc)
                        val authOk = mfc.authenticateSectorWithKeyA(sector.index, defaultKey) ||
                            mfc.authenticateSectorWithKeyB(sector.index, defaultKey)

                        if (authOk) {
                            mfc.writeBlock(absoluteBlock, block.data)
                            blocksWritten++
                        }
                    } catch (e: Exception) {
                        // Trailer block write may fail — that's OK on some cards
                        if (block.isTrailerBlock) {
                            // Try writing trailer anyway with different auth
                            try {
                                reconnect(mfc)
                                if (mfc.authenticateSectorWithKeyA(sector.index, defaultKey)) {
                                    mfc.writeBlock(absoluteBlock, block.data)
                                    blocksWritten++
                                }
                            } catch (_: Exception) {}
                        }
                        reconnect(mfc)
                    }
                }

                if (blocksWritten > 0) {
                    sectorsWritten++
                }
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

    private fun writeBlock0Magic(tag: Tag, mfc: MifareClassic, block0: ByteArray, defaultKey: ByteArray): Boolean {
        // Method 1: Direct write on CUID/Gen2 — authenticate then write block 0
        try {
            ensureConnected(mfc)
            if (mfc.authenticateSectorWithKeyA(0, defaultKey)) {
                mfc.writeBlock(0, block0)
                return true
            }
        } catch (_: Exception) {}

        // Method 2: Gen1a magic backdoor via NfcA raw commands
        try {
            try { mfc.close() } catch (_: Exception) {}
            val nfcA = NfcA.get(tag) ?: return false
            nfcA.connect()
            nfcA.timeout = 2000

            try {
                // HALT
                try { nfcA.transceive(byteArrayOf(0x50.toByte(), 0x00.toByte())) } catch (_: Exception) {}

                // Magic wakeup
                val response1 = nfcA.transceive(byteArrayOf(0x40.toByte()))
                if (response1.isNotEmpty()) {
                    nfcA.transceive(byteArrayOf(0x43.toByte()))

                    // Write block 0
                    nfcA.transceive(byteArrayOf(0xA0.toByte(), 0x00.toByte()))
                    nfcA.transceive(block0)

                    nfcA.close()
                    mfc.connect()
                    mfc.setTimeout(5000)
                    return true
                }
            } catch (_: Exception) {}

            try { nfcA.close() } catch (_: Exception) {}
            try { mfc.connect(); mfc.setTimeout(5000) } catch (_: Exception) {}
        } catch (_: Exception) {
            try { mfc.connect(); mfc.setTimeout(5000) } catch (_: Exception) {}
        }

        return false
    }

    private fun ensureConnected(mfc: MifareClassic) {
        if (!mfc.isConnected) {
            mfc.connect()
            mfc.setTimeout(5000)
        }
    }

    private fun reconnect(mfc: MifareClassic) {
        try { mfc.close() } catch (_: Exception) {}
        try {
            mfc.connect()
            mfc.setTimeout(5000)
        } catch (_: Exception) {}
    }
}
