package com.nfcemulator.nfc.hal

import com.nfcemulator.dump.model.EmulationCapabilities
import com.nfcemulator.dump.model.EmulationResult
import com.nfcemulator.dump.model.TagDump
import kotlinx.coroutines.flow.StateFlow

interface NfcEmulatorHal {
    val isEmulating: StateFlow<Boolean>
    suspend fun getCapabilities(): EmulationCapabilities
    suspend fun startEmulation(dump: TagDump): EmulationResult
    suspend fun stopEmulation(): EmulationResult
}
