package com.nfcemulator.nfc.emulator

import com.nfcemulator.dump.model.TagDump
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object EmulationState {
    private val _currentDump = MutableStateFlow<TagDump?>(null)
    val currentDump: StateFlow<TagDump?> = _currentDump.asStateFlow()

    private val _isEmulating = MutableStateFlow(false)
    val isEmulating: StateFlow<Boolean> = _isEmulating.asStateFlow()

    fun startEmulation(dump: TagDump) {
        _currentDump.value = dump
        _isEmulating.value = true
    }

    fun stopEmulation() {
        _currentDump.value = null
        _isEmulating.value = false
    }
}
