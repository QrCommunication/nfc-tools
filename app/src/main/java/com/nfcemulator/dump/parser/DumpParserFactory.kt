package com.nfcemulator.dump.parser

import com.nfcemulator.dump.model.TagDump
import java.io.InputStream

class DumpParserFactory {

    private val parsers: List<DumpParser> = listOf(
        MctParser(),
        MfdParser(),
        GenericDumpParser()
    )

    fun parse(inputStream: InputStream, fileName: String): TagDump {
        val parser = parsers.firstOrNull { it.canParse(fileName) }
            ?: throw IllegalArgumentException("No parser found for file: $fileName")
        return parser.parse(inputStream, fileName)
    }

    fun canParse(fileName: String): Boolean {
        return parsers.any { it.canParse(fileName) }
    }

    fun supportedExtensions(): List<String> {
        return listOf(".mfd", ".bin", ".mct", ".dump", ".json")
    }
}
