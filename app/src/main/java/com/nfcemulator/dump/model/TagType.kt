package com.nfcemulator.dump.model

enum class TagType(val displayName: String, val sectorCount: Int) {
    MIFARE_CLASSIC_1K("Mifare Classic 1K", 16),
    MIFARE_CLASSIC_4K("Mifare Classic 4K", 40),
    MIFARE_ULTRALIGHT("Mifare Ultralight", 0),
    MIFARE_ULTRALIGHT_C("Mifare Ultralight C", 0),
    MIFARE_DESFIRE("Mifare DESFire", 0),
    NTAG213("NTAG213", 0),
    NTAG215("NTAG215", 0),
    NTAG216("NTAG216", 0),
    ISO_14443_4("ISO 14443-4", 0),
    UNKNOWN("Unknown", 0)
}
