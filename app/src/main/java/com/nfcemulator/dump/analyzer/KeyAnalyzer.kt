package com.nfcemulator.dump.analyzer

import com.nfcemulator.dump.model.Block
import com.nfcemulator.dump.model.Sector
import com.nfcemulator.dump.model.TagDump

data class KeyAnalysis(
    val totalSectors: Int,
    val decryptedSectors: Int,
    val keysA: List<KeyInfo>,
    val keysB: List<KeyInfo>,
    val usesDefaultKeys: Boolean,
    val uniqueKeysCount: Int
)

data class KeyInfo(
    val sectorIndex: Int,
    val key: ByteArray?,
    val isDefault: Boolean,
    val keyHex: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is KeyInfo) return false
        return sectorIndex == other.sectorIndex && keyHex == other.keyHex
    }

    override fun hashCode(): Int = 31 * sectorIndex + keyHex.hashCode()
}

object KeyAnalyzer {

    private val DEFAULT_KEYS = setOf(
        "FFFFFFFFFFFF",
        "A0A1A2A3A4A5",
        "B0B1B2B3B4B5",
        "D3F7D3F7D3F7",
        "000000000000",
        "4D3A99C351DD",
        "1A982C7E459A",
        "AABBCCDDEEFF"
    )

    fun analyze(dump: TagDump): KeyAnalysis {
        val keysA = dump.sectors.map { sector ->
            val keyBytes = sector.keyA
            val keyHex = keyBytes?.joinToString("") { "%02X".format(it) } ?: ""
            KeyInfo(
                sectorIndex = sector.index,
                key = keyBytes,
                isDefault = keyHex.uppercase() in DEFAULT_KEYS,
                keyHex = keyHex
            )
        }

        val keysB = dump.sectors.map { sector ->
            val keyBytes = sector.keyB
            val keyHex = keyBytes?.joinToString("") { "%02X".format(it) } ?: ""
            KeyInfo(
                sectorIndex = sector.index,
                key = keyBytes,
                isDefault = keyHex.uppercase() in DEFAULT_KEYS,
                keyHex = keyHex
            )
        }

        val allKeyHexes = (keysA + keysB).filter { it.key != null }.map { it.keyHex.uppercase() }.toSet()

        return KeyAnalysis(
            totalSectors = dump.sectors.size,
            decryptedSectors = dump.sectors.count { it.isDecrypted },
            keysA = keysA,
            keysB = keysB,
            usesDefaultKeys = allKeyHexes.any { it in DEFAULT_KEYS },
            uniqueKeysCount = allKeyHexes.size
        )
    }

    fun compareDumps(dump1: TagDump, dump2: TagDump): List<DiffEntry> {
        val diffs = mutableListOf<DiffEntry>()
        val maxSectors = maxOf(dump1.sectors.size, dump2.sectors.size)

        for (sectorIndex in 0 until maxSectors) {
            val sector1 = dump1.sectors.getOrNull(sectorIndex)
            val sector2 = dump2.sectors.getOrNull(sectorIndex)

            if (sector1 == null || sector2 == null) {
                diffs.add(DiffEntry(sectorIndex, -1, DiffType.MISSING_SECTOR))
                continue
            }

            val maxBlocks = maxOf(sector1.blocks.size, sector2.blocks.size)
            for (blockIndex in 0 until maxBlocks) {
                val block1 = sector1.blocks.getOrNull(blockIndex)
                val block2 = sector2.blocks.getOrNull(blockIndex)

                if (block1 == null || block2 == null) {
                    diffs.add(DiffEntry(sectorIndex, blockIndex, DiffType.MISSING_BLOCK))
                } else if (!block1.data.contentEquals(block2.data)) {
                    diffs.add(DiffEntry(sectorIndex, blockIndex, DiffType.DATA_CHANGED,
                        oldData = block1.data, newData = block2.data))
                }
            }
        }

        return diffs
    }
}

data class DiffEntry(
    val sectorIndex: Int,
    val blockIndex: Int,
    val type: DiffType,
    val oldData: ByteArray? = null,
    val newData: ByteArray? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DiffEntry) return false
        return sectorIndex == other.sectorIndex && blockIndex == other.blockIndex && type == other.type
    }

    override fun hashCode(): Int = 31 * (31 * sectorIndex + blockIndex) + type.hashCode()
}

enum class DiffType { DATA_CHANGED, MISSING_SECTOR, MISSING_BLOCK }
