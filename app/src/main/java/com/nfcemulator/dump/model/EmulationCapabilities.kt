package com.nfcemulator.dump.model

data class EmulationCapabilities(
    val hasNfc: Boolean,
    val hasHce: Boolean,
    val hasRoot: Boolean,
    val hasNxpChipset: Boolean,
    val supportedTypes: List<TagType>
) {
    val canEmulateMifareClassic: Boolean
        get() = hasRoot && hasNxpChipset

    val canEmulateIsoDep: Boolean
        get() = hasHce

    val emulationMode: EmulationMode
        get() = when {
            canEmulateMifareClassic -> EmulationMode.ROOT_NXP
            canEmulateIsoDep -> EmulationMode.HCE_STANDARD
            else -> EmulationMode.NONE
        }
}

enum class EmulationMode(val displayName: String) {
    ROOT_NXP("Root NXP (Full)"),
    HCE_STANDARD("HCE Standard (Limited)"),
    NONE("No Emulation")
}
