package com.nfcemulator.nfc.reader

import android.nfc.Tag
import android.nfc.tech.MifareClassic
import android.nfc.tech.MifareUltralight
import android.nfc.tech.NfcA
import com.nfcemulator.dump.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

sealed class ReadProgress {
    data object Idle : ReadProgress()
    data class Reading(val sector: Int, val total: Int) : ReadProgress()
    data class KeyTesting(val sector: Int, val keysTestedCount: Int) : ReadProgress()
    data class Complete(val dump: TagDump) : ReadProgress()
    data class Error(val message: String) : ReadProgress()
}

class TagReader {

    private val _progress = MutableStateFlow<ReadProgress>(ReadProgress.Idle)
    val progress: StateFlow<ReadProgress> = _progress.asStateFlow()

    suspend fun readTag(tag: Tag, keys: List<ByteArray> = emptyList()): TagDump? {
        return withContext(Dispatchers.IO) {
            try {
                val tagType = TagTypeDetector.detect(tag)
                val uid = tag.id
                val nfcA = NfcA.get(tag)
                val atqa = nfcA?.atqa ?: byteArrayOf()
                val sak = nfcA?.sak?.toByte() ?: 0

                val sectors = when (tagType) {
                    TagType.MIFARE_CLASSIC_1K, TagType.MIFARE_CLASSIC_4K -> {
                        readMifareClassic(tag, tagType, keys)
                    }
                    TagType.MIFARE_ULTRALIGHT, TagType.MIFARE_ULTRALIGHT_C -> {
                        readMifareUltralight(tag)
                    }
                    else -> emptyList()
                }

                val dump = TagDump(
                    name = "${tagType.displayName} ${uid.joinToString("") { "%02X".format(it) }.takeLast(8)}",
                    type = tagType,
                    uid = uid,
                    atqa = atqa,
                    sak = sak,
                    sectors = sectors,
                    sourceFormat = DumpFormat.NFC_READ
                )

                _progress.value = ReadProgress.Complete(dump)
                dump
            } catch (e: Exception) {
                _progress.value = ReadProgress.Error(e.message ?: "Unknown error")
                null
            }
        }
    }

    private fun readMifareClassic(
        tag: Tag,
        tagType: TagType,
        keys: List<ByteArray>
    ): List<Sector> {
        val mfc = MifareClassic.get(tag) ?: return emptyList()
        val sectors = mutableListOf<Sector>()

        try {
            mfc.connect()
            mfc.setTimeout(2000)
            val sectorCount = mfc.sectorCount

            for (sectorIndex in 0 until sectorCount) {
                _progress.value = ReadProgress.Reading(sectorIndex, sectorCount)

                var foundKeyA: ByteArray? = null
                var foundKeyB: ByteArray? = null

                // === FIND KEY A ===
                foundKeyA = tryAuthenticateA(mfc, sectorIndex, keys)

                // === FIND KEY B (ALWAYS, even if Key A was found) ===
                foundKeyB = tryAuthenticateB(mfc, sectorIndex, keys)

                // === READ BLOCKS ===
                val blockCount = mfc.getBlockCountInSector(sectorIndex)
                val firstBlock = mfc.sectorToBlock(sectorIndex)
                val blocks = mutableListOf<Block>()

                for (blockIndex in 0 until blockCount) {
                    val absoluteBlock = firstBlock + blockIndex
                    var data = ByteArray(16)

                    if (foundKeyA != null) {
                        data = readBlockSafe(mfc, sectorIndex, absoluteBlock, foundKeyA, isKeyA = true)
                    } else if (foundKeyB != null) {
                        data = readBlockSafe(mfc, sectorIndex, absoluteBlock, foundKeyB, isKeyA = false)
                    }

                    val isTrailer = blockIndex == blockCount - 1
                    blocks.add(
                        Block(
                            index = absoluteBlock,
                            data = data,
                            keyA = if (isTrailer) foundKeyA else null,
                            keyB = if (isTrailer) foundKeyB else null,
                            accessBits = if (isTrailer && data.size >= 10) data.sliceArray(6..9) else null
                        )
                    )
                }

                sectors.add(Sector(index = sectorIndex, blocks = blocks))
            }
        } catch (_: Exception) {
            // Return what we have so far
        } finally {
            try { mfc.close() } catch (_: Exception) {}
        }

        return sectors
    }

    private fun tryAuthenticateA(
        mfc: MifareClassic,
        sectorIndex: Int,
        keys: List<ByteArray>
    ): ByteArray? {
        for ((idx, key) in keys.withIndex()) {
            if (idx % 50 == 0) {
                _progress.value = ReadProgress.KeyTesting(sectorIndex, idx)
            }
            try {
                ensureConnected(mfc)
                if (mfc.authenticateSectorWithKeyA(sectorIndex, key)) {
                    return key.copyOf()
                }
            } catch (_: Exception) {
                reconnect(mfc)
            }
        }
        return null
    }

    private fun tryAuthenticateB(
        mfc: MifareClassic,
        sectorIndex: Int,
        keys: List<ByteArray>
    ): ByteArray? {
        for (key in keys) {
            try {
                ensureConnected(mfc)
                if (mfc.authenticateSectorWithKeyB(sectorIndex, key)) {
                    return key.copyOf()
                }
            } catch (_: Exception) {
                reconnect(mfc)
            }
        }
        return null
    }

    private fun readBlockSafe(
        mfc: MifareClassic,
        sectorIndex: Int,
        blockIndex: Int,
        key: ByteArray,
        isKeyA: Boolean
    ): ByteArray {
        return try {
            ensureConnected(mfc)
            if (isKeyA) {
                mfc.authenticateSectorWithKeyA(sectorIndex, key)
            } else {
                mfc.authenticateSectorWithKeyB(sectorIndex, key)
            }
            mfc.readBlock(blockIndex)
        } catch (_: Exception) {
            reconnect(mfc)
            ByteArray(16)
        }
    }

    private fun ensureConnected(mfc: MifareClassic) {
        if (!mfc.isConnected) {
            mfc.connect()
            mfc.setTimeout(2000)
        }
    }

    private fun reconnect(mfc: MifareClassic) {
        try { mfc.close() } catch (_: Exception) {}
        try {
            mfc.connect()
            mfc.setTimeout(2000)
        } catch (_: Exception) {}
    }

    private fun readMifareUltralight(tag: Tag): List<Sector> {
        val ul = MifareUltralight.get(tag) ?: return emptyList()
        val blocks = mutableListOf<Block>()

        try {
            ul.connect()
            val maxPages = when (ul.type) {
                MifareUltralight.TYPE_ULTRALIGHT -> 16
                MifareUltralight.TYPE_ULTRALIGHT_C -> 48
                else -> 16
            }

            for (page in 0 until maxPages step 4) {
                try {
                    val data = ul.readPages(page)
                    for (i in 0 until minOf(4, maxPages - page)) {
                        if (data.size >= (i + 1) * 4) {
                            blocks.add(
                                Block(
                                    index = page + i,
                                    data = data.sliceArray(i * 4 until (i + 1) * 4)
                                )
                            )
                        }
                    }
                } catch (_: Exception) {
                    break
                }
            }
        } finally {
            try { ul.close() } catch (_: Exception) {}
        }

        return if (blocks.isNotEmpty()) listOf(Sector(index = 0, blocks = blocks)) else emptyList()
    }

    fun reset() {
        _progress.value = ReadProgress.Idle
    }
}
