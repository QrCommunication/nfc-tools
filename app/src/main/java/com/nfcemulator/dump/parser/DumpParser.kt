package com.nfcemulator.dump.parser

import com.nfcemulator.dump.model.TagDump
import java.io.InputStream

interface DumpParser {
    fun canParse(fileName: String): Boolean
    fun parse(inputStream: InputStream, fileName: String): TagDump
}
