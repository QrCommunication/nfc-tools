package com.nfcemulator.nfc.hal

import android.content.Context
import com.nfcemulator.dump.model.EmulationCapabilities
import com.nfcemulator.dump.model.EmulationResult
import com.nfcemulator.dump.model.TagDump
import com.nfcemulator.dump.model.TagType
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class RootNxpEmulatorHal(
    private val context: Context
) : NfcEmulatorHal {

    private val _isEmulating = MutableStateFlow(false)
    override val isEmulating: StateFlow<Boolean> = _isEmulating.asStateFlow()

    private var currentDump: TagDump? = null

    override suspend fun getCapabilities(): EmulationCapabilities {
        val nfcAdapter = android.nfc.NfcAdapter.getDefaultAdapter(context)
        val hasRoot = withContext(Dispatchers.IO) {
            Shell.isAppGrantedRoot() == true
        }
        val hasNxp = hasRoot && checkNxpChipset()

        val supportedTypes = mutableListOf(TagType.ISO_14443_4)
        if (hasNxp) {
            supportedTypes.addAll(listOf(
                TagType.MIFARE_CLASSIC_1K,
                TagType.MIFARE_CLASSIC_4K,
                TagType.MIFARE_ULTRALIGHT,
                TagType.MIFARE_DESFIRE
            ))
        }

        return EmulationCapabilities(
            hasNfc = nfcAdapter != null,
            hasHce = nfcAdapter?.isEnabled == true,
            hasRoot = hasRoot,
            hasNxpChipset = hasNxp,
            supportedTypes = supportedTypes
        )
    }

    override suspend fun startEmulation(dump: TagDump): EmulationResult {
        val capabilities = getCapabilities()
        if (!capabilities.hasRoot) {
            return EmulationResult.Error("Root access required for Mifare Classic emulation")
        }
        if (!capabilities.hasNxpChipset) {
            return EmulationResult.Error("NXP chipset not detected. Mifare Classic emulation requires NXP NFC hardware")
        }

        return withContext(Dispatchers.IO) {
            try {
                val uidHex = dump.uid.joinToString("") { "%02X".format(it) }
                val result = Shell.cmd(
                    "echo $uidHex > /sys/class/nfc/nfc0/uid_emulation"
                ).exec()

                if (result.isSuccess) {
                    currentDump = dump
                    _isEmulating.value = true
                    EmulationResult.Started
                } else {
                    EmulationResult.Error("Failed to configure NFC emulation: ${result.err.joinToString()}")
                }
            } catch (e: Exception) {
                EmulationResult.Error("Emulation failed: ${e.message}", e)
            }
        }
    }

    override suspend fun stopEmulation(): EmulationResult {
        return withContext(Dispatchers.IO) {
            try {
                Shell.cmd("echo 0 > /sys/class/nfc/nfc0/uid_emulation").exec()
                currentDump = null
                _isEmulating.value = false
                EmulationResult.Stopped
            } catch (e: Exception) {
                EmulationResult.Error("Failed to stop emulation: ${e.message}", e)
            }
        }
    }

    private suspend fun checkNxpChipset(): Boolean = withContext(Dispatchers.IO) {
        try {
            val result = Shell.cmd("ls /sys/class/nfc/nfc0/").exec()
            result.isSuccess && result.out.any { it.contains("nxp", ignoreCase = true) || it.contains("pn5", ignoreCase = true) }
        } catch (_: Exception) {
            false
        }
    }

    fun getCurrentDump(): TagDump? = currentDump
}
