package com.nfcemulator.dump.parser

import com.nfcemulator.dump.model.DumpFormat
import com.nfcemulator.dump.model.TagDump
import java.io.InputStream

class GenericDumpParser : DumpParser {

    private val mfdParser = MfdParser()

    override fun canParse(fileName: String): Boolean {
        return fileName.lowercase().endsWith(".dump")
    }

    override fun parse(inputStream: InputStream, fileName: String): TagDump {
        val dump = mfdParser.parse(inputStream, fileName)
        return dump.copy(sourceFormat = DumpFormat.DUMP)
    }
}
