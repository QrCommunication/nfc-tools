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
            val sectorCount = mfc.sectorCount

            for (sectorIndex in 0 until sectorCount) {
                _progress.value = ReadProgress.Reading(sectorIndex, sectorCount)

                var authenticatedKeyA: ByteArray? = null
                var authenticatedKeyB: ByteArray? = null
                val blocks = mutableListOf<Block>()

                // Try default key first
                val defaultKey = byteArrayOf(0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte())
                if (mfc.authenticateSectorWithKeyA(sectorIndex, defaultKey)) {
                    authenticatedKeyA = defaultKey
                } else {
                    for ((idx, key) in keys.withIndex()) {
                        _progress.value = ReadProgress.KeyTesting(sectorIndex, idx + 1)
                        if (mfc.authenticateSectorWithKeyA(sectorIndex, key)) {
                            authenticatedKeyA = key
                            break
                        }
                    }
                }

                if (authenticatedKeyA == null) {
                    if (mfc.authenticateSectorWithKeyB(sectorIndex, defaultKey)) {
                        authenticatedKeyB = defaultKey
                    } else {
                        for (key in keys) {
                            if (mfc.authenticateSectorWithKeyB(sectorIndex, key)) {
                                authenticatedKeyB = key
                                break
                            }
                        }
                    }
                }

                val blockCount = mfc.getBlockCountInSector(sectorIndex)
                val firstBlock = mfc.sectorToBlock(sectorIndex)

                for (blockIndex in 0 until blockCount) {
                    val absoluteBlock = firstBlock + blockIndex
                    val data = if (authenticatedKeyA != null || authenticatedKeyB != null) {
                        try {
                            mfc.readBlock(absoluteBlock)
                        } catch (_: Exception) {
                            ByteArray(16)
                        }
                    } else {
                        ByteArray(16)
                    }

                    val isTrailer = blockIndex == blockCount - 1
                    blocks.add(
                        Block(
                            index = absoluteBlock,
                            data = data,
                            keyA = if (isTrailer) authenticatedKeyA else null,
                            keyB = if (isTrailer) authenticatedKeyB else null,
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
