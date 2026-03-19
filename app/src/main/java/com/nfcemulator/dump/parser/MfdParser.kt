package com.nfcemulator.dump.parser

import com.nfcemulator.dump.model.*
import java.io.InputStream

class MfdParser : DumpParser {

    override fun canParse(fileName: String): Boolean {
        val ext = fileName.lowercase()
        return ext.endsWith(".mfd") || ext.endsWith(".bin")
    }

    override fun parse(inputStream: InputStream, fileName: String): TagDump {
        val rawData = inputStream.readBytes()
        val tagType = when (rawData.size) {
            1024 -> TagType.MIFARE_CLASSIC_1K
            4096 -> TagType.MIFARE_CLASSIC_4K
            else -> if (rawData.size <= 1024) TagType.MIFARE_CLASSIC_1K else TagType.MIFARE_CLASSIC_4K
        }

        val sectorCount = tagType.sectorCount
        val sectors = mutableListOf<Sector>()
        var offset = 0

        for (sectorIndex in 0 until sectorCount) {
            val blocksInSector = if (sectorIndex < 32) 4 else 16
            val blocks = mutableListOf<Block>()

            for (blockIndex in 0 until blocksInSector) {
                val blockSize = 16
                val data = if (offset + blockSize <= rawData.size) {
                    rawData.sliceArray(offset until offset + blockSize)
                } else {
                    ByteArray(blockSize)
                }

                val absoluteBlockIndex = if (sectorIndex < 32) {
                    sectorIndex * 4 + blockIndex
                } else {
                    128 + (sectorIndex - 32) * 16 + blockIndex
                }

                val isTrailer = blockIndex == blocksInSector - 1
                blocks.add(
                    Block(
                        index = absoluteBlockIndex,
                        data = data,
                        keyA = if (isTrailer && data.size >= 6) data.sliceArray(0..5) else null,
                        keyB = if (isTrailer && data.size >= 16) data.sliceArray(10..15) else null,
                        accessBits = if (isTrailer && data.size >= 10) data.sliceArray(6..9) else null
                    )
                )
                offset += blockSize
            }

            sectors.add(Sector(index = sectorIndex, blocks = blocks))
        }

        val uid = if (rawData.size >= 4) rawData.sliceArray(0..3) else byteArrayOf()

        return TagDump(
            name = fileName.substringBeforeLast('.'),
            type = tagType,
            uid = uid,
            sectors = sectors,
            rawData = rawData,
            sourceFormat = if (fileName.lowercase().endsWith(".mfd")) DumpFormat.MFD else DumpFormat.BIN
        )
    }
}
