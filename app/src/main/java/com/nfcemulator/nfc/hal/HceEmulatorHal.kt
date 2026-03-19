package com.nfcemulator.nfc.hal

import android.content.Context
import com.nfcemulator.dump.model.EmulationCapabilities
import com.nfcemulator.dump.model.EmulationMode
import com.nfcemulator.dump.model.EmulationResult
import com.nfcemulator.dump.model.TagDump
import com.nfcemulator.dump.model.TagType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HceEmulatorHal(
    private val context: Context
) : NfcEmulatorHal {

    private val _isEmulating = MutableStateFlow(false)
    override val isEmulating: StateFlow<Boolean> = _isEmulating.asStateFlow()

    private var currentDump: TagDump? = null

    override suspend fun getCapabilities(): EmulationCapabilities {
        val nfcAdapter = android.nfc.NfcAdapter.getDefaultAdapter(context)
        return EmulationCapabilities(
            hasNfc = nfcAdapter != null,
            hasHce = nfcAdapter?.isEnabled == true,
            hasRoot = false,
            hasNxpChipset = false,
            supportedTypes = listOf(TagType.ISO_14443_4)
        )
    }

    override suspend fun startEmulation(dump: TagDump): EmulationResult {
        val capabilities = getCapabilities()
        if (!capabilities.hasHce) {
            return EmulationResult.Error("HCE not available on this device")
        }

        if (dump.type != TagType.ISO_14443_4 && dump.type != TagType.MIFARE_DESFIRE) {
            return EmulationResult.UnsupportedType(
                tagType = dump.type,
                requiredMode = EmulationMode.ROOT_NXP
            )
        }

        currentDump = dump
        _isEmulating.value = true
        return EmulationResult.Started
    }

    override suspend fun stopEmulation(): EmulationResult {
        currentDump = null
        _isEmulating.value = false
        return EmulationResult.Stopped
    }

    fun getCurrentDump(): TagDump? = currentDump
}
