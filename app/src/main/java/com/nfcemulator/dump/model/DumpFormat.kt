package com.nfcemulator.dump.model

enum class DumpFormat(val extension: String, val displayName: String) {
    MFD(".mfd", "Raw Binary (MFD)"),
    BIN(".bin", "Raw Binary (BIN)"),
    MCT(".mct", "MIFARE Classic Tool"),
    DUMP(".dump", "Generic Dump"),
    JSON(".json", "NFC Emulator JSON"),
    NFC_READ("", "NFC Direct Read")
}
