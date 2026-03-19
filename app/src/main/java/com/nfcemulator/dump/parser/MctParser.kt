package com.nfcemulator.dump.parser

import com.nfcemulator.dump.model.*
import java.io.InputStream

class MctParser : DumpParser {

    override fun canParse(fileName: String): Boolean {
        return fileName.lowercase().endsWith(".mct")
    }

    override fun parse(inputStream: InputStream, fileName: String): TagDump {
        val content = inputStream.bufferedReader().readText()
        val lines = content.lines().filter { it.isNotBlank() }

        val sectors = mutableListOf<Sector>()
        var currentSectorIndex = -1
        var currentBlocks = mutableListOf<Block>()
        var absoluteBlockIndex = 0

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.startsWith("+Sector:")) {
                if (currentSectorIndex >= 0 && currentBlocks.isNotEmpty()) {
                    sectors.add(Sector(index = currentSectorIndex, blocks = currentBlocks.toList()))
                }
                currentSectorIndex = trimmed.substringAfter("+Sector:").trim().toIntOrNull() ?: (currentSectorIndex + 1)
                currentBlocks = mutableListOf()
            } else if (trimmed.length >= 32 && trimmed.all { it in "0123456789ABCDEFabcdef-" }) {
                val hexClean = trimmed.replace("-", "0")
                val data = hexStringToByteArray(hexClean)
                val blocksInSector = if (currentSectorIndex < 32) 4 else 16
                val blockIndexInSector = currentBlocks.size
                val isTrailer = blockIndexInSector == blocksInSector - 1

                currentBlocks.add(
                    Block(
                        index = absoluteBlockIndex,
                        data = data,
                        keyA = if (isTrailer && data.size >= 6) data.sliceArray(0..5) else null,
                        keyB = if (isTrailer && data.size >= 16) data.sliceArray(10..15) else null,
                        accessBits = if (isTrailer && data.size >= 10) data.sliceArray(6..9) else null
                    )
                )
                absoluteBlockIndex++
            }
        }

        if (currentSectorIndex >= 0 && currentBlocks.isNotEmpty()) {
            sectors.add(Sector(index = currentSectorIndex, blocks = currentBlocks.toList()))
        }

        val tagType = when {
            sectors.size <= 16 -> TagType.MIFARE_CLASSIC_1K
            else -> TagType.MIFARE_CLASSIC_4K
        }

        val uid = sectors.firstOrNull()?.blocks?.firstOrNull()?.data?.sliceArray(0..3) ?: byteArrayOf()

        return TagDump(
            name = fileName.substringBeforeLast('.'),
            type = tagType,
            uid = uid,
            sectors = sectors,
            sourceFormat = DumpFormat.MCT
        )
    }

    private fun hexStringToByteArray(hex: String): ByteArray {
        val cleanHex = hex.replace(" ", "")
        return ByteArray(cleanHex.length / 2) { i ->
            cleanHex.substring(i * 2, i * 2 + 2).toInt(16).toByte()
        }
    }
}
