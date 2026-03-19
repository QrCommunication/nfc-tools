package com.nfcemulator.dump.model

sealed class EmulationResult {
    data object Started : EmulationResult()
    data object Stopped : EmulationResult()
    data class Error(val message: String, val cause: Throwable? = null) : EmulationResult()
    data class UnsupportedType(val tagType: TagType, val requiredMode: EmulationMode) : EmulationResult()
}
