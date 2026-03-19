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
import java.io.File

class RootNxpEmulatorHal(
    private val context: Context
) : NfcEmulatorHal {

    private val _isEmulating = MutableStateFlow(false)
    override val isEmulating: StateFlow<Boolean> = _isEmulating.asStateFlow()

    private var currentDump: TagDump? = null
    private var cachedCapabilities: EmulationCapabilities? = null

    override suspend fun getCapabilities(): EmulationCapabilities {
        cachedCapabilities?.let { return it }

        val nfcAdapter = android.nfc.NfcAdapter.getDefaultAdapter(context)
        val hasRoot = withContext(Dispatchers.IO) { checkRoot() }
        val hasNxp = if (hasRoot) withContext(Dispatchers.IO) { checkNxpChipset() } else false

        val supportedTypes = mutableListOf(TagType.ISO_14443_4)
        if (hasNxp) {
            supportedTypes.addAll(listOf(
                TagType.MIFARE_CLASSIC_1K,
                TagType.MIFARE_CLASSIC_4K,
                TagType.MIFARE_ULTRALIGHT,
                TagType.MIFARE_DESFIRE
            ))
        }

        val capabilities = EmulationCapabilities(
            hasNfc = nfcAdapter != null,
            hasHce = nfcAdapter?.isEnabled == true,
            hasRoot = hasRoot,
            hasNxpChipset = hasNxp,
            supportedTypes = supportedTypes
        )
        cachedCapabilities = capabilities
        return capabilities
    }

    fun invalidateCache() {
        cachedCapabilities = null
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

    private fun checkRoot(): Boolean {
        // Method 1: libsu Shell check
        try {
            val shellResult = Shell.isAppGrantedRoot()
            if (shellResult == true) return true
        } catch (_: Exception) {}

        // Method 2: Check su binary exists
        val suPaths = listOf(
            "/system/bin/su",
            "/system/xbin/su",
            "/sbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/su/bin/su",
            "/data/adb/su",
            "/apex/com.android.runtime/bin/su"
        )
        for (path in suPaths) {
            if (File(path).exists()) return true
        }

        // Method 3: Try executing su
        try {
            val process = Runtime.getRuntime().exec("su -c id")
            val exitCode = process.waitFor()
            if (exitCode == 0) return true
        } catch (_: Exception) {}

        // Method 4: Check Magisk
        try {
            val magiskPaths = listOf(
                "/data/adb/magisk",
                "/sbin/.magisk",
                "/data/adb/modules"
            )
            for (path in magiskPaths) {
                if (File(path).exists()) return true
            }
        } catch (_: Exception) {}

        // Method 5: Check build tags
        try {
            val buildTags = android.os.Build.TAGS
            if (buildTags != null && buildTags.contains("test-keys")) return true
        } catch (_: Exception) {}

        // Method 6: Check common root apps
        try {
            val pm = context.packageManager
            val rootPackages = listOf(
                "com.topjohnwu.magisk",
                "io.github.vvb2060.magisk",
                "com.fox2code.mmm",
                "eu.chainfire.supersu",
                "com.noshufou.android.su",
                "com.koushikdutta.superuser",
                "com.zachspong.temprootremovejb",
                "com.ramdroid.appquarantine",
                "me.phh.superuser",
                "com.topjohnwu.magisk.lite"
            )
            for (pkg in rootPackages) {
                try {
                    pm.getPackageInfo(pkg, 0)
                    return true
                } catch (_: Exception) {}
            }
        } catch (_: Exception) {}

        return false
    }

    private fun checkNxpChipset(): Boolean {
        // Method 1: Check sysfs
        try {
            val result = Shell.cmd("ls /sys/class/nfc/nfc0/ 2>/dev/null").exec()
            if (result.isSuccess && result.out.any {
                it.contains("nxp", ignoreCase = true) || it.contains("pn5", ignoreCase = true)
            }) return true
        } catch (_: Exception) {}

        // Method 2: Check NFC chipset via getprop
        try {
            val result = Shell.cmd("getprop | grep -i nfc").exec()
            if (result.isSuccess && result.out.any {
                it.contains("nxp", ignoreCase = true) ||
                it.contains("pn54", ignoreCase = true) ||
                it.contains("pn55", ignoreCase = true) ||
                it.contains("pn65", ignoreCase = true) ||
                it.contains("pn66", ignoreCase = true) ||
                it.contains("pn67", ignoreCase = true) ||
                it.contains("pn80", ignoreCase = true) ||
                it.contains("pn81", ignoreCase = true) ||
                it.contains("sn1", ignoreCase = true) ||
                it.contains("sn2", ignoreCase = true)
            }) return true
        } catch (_: Exception) {}

        // Method 3: Check for libnfc-nci or libnfc-nxp
        try {
            val libPaths = listOf(
                "/system/lib64/libnfc-nci.so",
                "/system/lib/libnfc-nci.so",
                "/vendor/lib64/libnfc-nci.so",
                "/vendor/lib/libnfc-nci.so",
                "/system/lib64/libnfc_nci_jni.so",
                "/vendor/lib64/nfc_nci.nxp.default.so"
            )
            for (path in libPaths) {
                if (File(path).exists()) return true
            }
        } catch (_: Exception) {}

        // Method 4: Check device hardware info
        try {
            val result = Shell.cmd("cat /proc/cpuinfo 2>/dev/null; getprop ro.hardware.nfc 2>/dev/null").exec()
            if (result.isSuccess && result.out.any {
                it.contains("nxp", ignoreCase = true) || it.contains("nq", ignoreCase = true)
            }) return true
        } catch (_: Exception) {}

        return false
    }

    fun getCurrentDump(): TagDump? = currentDump
}
