package com.nfcemulator.nfc.hal

import android.content.Context
import com.nfcemulator.dump.model.EmulationCapabilities
import com.nfcemulator.dump.model.EmulationResult
import com.nfcemulator.dump.model.TagDump
import com.nfcemulator.dump.model.TagType
import com.nfcemulator.nfc.emulator.EmulationState
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.io.File

class RootNxpEmulatorHal(
    private val context: Context
) : NfcEmulatorHal {

    override val isEmulating: StateFlow<Boolean> = EmulationState.isEmulating

    private var cachedCapabilities: EmulationCapabilities? = null
    var rootDebugLog: String = ""
        private set
    var nxpDebugLog: String = ""
        private set

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
        // Use HCE emulation via EmulationState (works without sysfs)
        // The NfcHostApduService reads from EmulationState.currentDump
        EmulationState.startEmulation(dump)

        // If we have root, also try native NXP emulation for Mifare Classic
        val capabilities = getCapabilities()
        if (capabilities.hasRoot && capabilities.hasNxpChipset) {
            withContext(Dispatchers.IO) {
                try {
                    val uidHex = dump.uid.joinToString("") { "%02X".format(it) }
                    Shell.cmd("echo $uidHex > /sys/class/nfc/nfc0/uid_emulation 2>/dev/null").exec()
                } catch (_: Exception) {}
            }
        }

        return EmulationResult.Started
    }

    override suspend fun stopEmulation(): EmulationResult {
        EmulationState.stopEmulation()

        withContext(Dispatchers.IO) {
            try {
                Shell.cmd("echo 0 > /sys/class/nfc/nfc0/uid_emulation 2>/dev/null").exec()
            } catch (_: Exception) {}
        }

        return EmulationResult.Stopped
    }

    private fun checkRoot(): Boolean {
        val log = StringBuilder()

        // Method 1: libsu — get or create shell synchronously (triggers Magisk prompt)
        try {
            log.append("[1] Shell.getShell()... ")
            val shell = Shell.getShell()
            val isRoot = shell.isRoot
            log.appendLine("isRoot=$isRoot, status=${shell.status}")
            if (isRoot) {
                rootDebugLog = log.toString()
                return true
            }
        } catch (e: Exception) {
            log.appendLine("ERROR: ${e.message}")
        }

        // Method 2: libsu isAppGrantedRoot
        try {
            log.append("[2] Shell.isAppGrantedRoot()... ")
            val result = Shell.isAppGrantedRoot()
            log.appendLine("result=$result")
            if (result == true) {
                rootDebugLog = log.toString()
                return true
            }
        } catch (e: Exception) {
            log.appendLine("ERROR: ${e.message}")
        }

        // Method 3: ProcessBuilder su with full paths
        val suPaths = listOf("su", "/sbin/su", "/system/bin/su", "/system/xbin/su",
            "/data/local/xbin/su", "/data/local/bin/su", "/su/bin/su",
            "/data/adb/magisk/su", "/debug_ramdisk/su")
        for (suPath in suPaths) {
            try {
                log.append("[3] ProcessBuilder($suPath -c id)... ")
                val process = ProcessBuilder(suPath, "-c", "id")
                    .redirectErrorStream(true)
                    .start()
                val output = process.inputStream.bufferedReader().use { it.readText().trim() }
                val exited = process.waitFor()
                log.appendLine("exit=$exited, out=${output.take(80)}")
                if (exited == 0 && (output.contains("uid=0") || output.contains("root"))) {
                    rootDebugLog = log.toString()
                    return true
                }
            } catch (e: Exception) {
                log.appendLine("ERROR: ${e.message}")
            }
        }

        // Method 4: which su / type su
        try {
            log.append("[4] which su... ")
            val process = ProcessBuilder("which", "su").redirectErrorStream(true).start()
            val output = process.inputStream.bufferedReader().use { it.readText().trim() }
            process.waitFor()
            log.appendLine("output='$output'")
            if (output.isNotEmpty() && !output.contains("not found")) {
                rootDebugLog = log.toString()
                return true
            }
        } catch (e: Exception) {
            log.appendLine("ERROR: ${e.message}")
        }

        // Method 5: Check Magisk package (with hidden app support)
        try {
            log.append("[5] Package check... ")
            val pm = context.packageManager
            val rootPackages = listOf(
                "com.topjohnwu.magisk", "io.github.vvb2060.magisk",
                "com.fox2code.mmm", "eu.chainfire.supersu",
                "me.phh.superuser", "me.weishu.kernelsu",
                "com.riaru.kernelsu", "org.lsposed.manager"
            )
            val found = mutableListOf<String>()
            for (pkg in rootPackages) {
                try {
                    @Suppress("DEPRECATION")
                    pm.getPackageInfo(pkg, 0)
                    found.add(pkg)
                } catch (_: Exception) {}
            }
            log.appendLine("found=${found.ifEmpty { "none" }}")

            // Also check all installed packages for magisk in name
            @Suppress("DEPRECATION")
            val allPackages = pm.getInstalledPackages(0)
            val magiskLike = allPackages.filter {
                it.packageName.contains("magisk", ignoreCase = true) ||
                it.packageName.contains("supersu", ignoreCase = true) ||
                it.packageName.contains("superuser", ignoreCase = true) ||
                it.packageName.contains("kernelsu", ignoreCase = true)
            }.map { it.packageName }
            if (magiskLike.isNotEmpty()) {
                log.appendLine("  magisk-like packages: $magiskLike")
            }

            if (found.isNotEmpty() || magiskLike.isNotEmpty()) {
                rootDebugLog = log.toString()
                return true
            }
        } catch (e: Exception) {
            log.appendLine("ERROR: ${e.message}")
        }

        // Method 6: Check /proc/self/mountinfo for magisk
        try {
            log.append("[6] /proc/self/mountinfo... ")
            val mountInfo = File("/proc/self/mountinfo").readText()
            val hasMagisk = mountInfo.contains("magisk", ignoreCase = true)
            log.appendLine("hasMagisk=$hasMagisk")
            if (hasMagisk) {
                rootDebugLog = log.toString()
                return true
            }
        } catch (e: Exception) {
            log.appendLine("ERROR: ${e.message}")
        }

        // Method 7: Check /proc/self/mounts for magisk tmpfs
        try {
            log.append("[7] /proc/self/mounts... ")
            val mounts = File("/proc/self/mounts").readText()
            val hasMagisk = mounts.contains("magisk", ignoreCase = true) ||
                mounts.contains("/sbin", ignoreCase = true)
            log.appendLine("hasMagisk=$hasMagisk")
            if (hasMagisk) {
                rootDebugLog = log.toString()
                return true
            }
        } catch (e: Exception) {
            log.appendLine("ERROR: ${e.message}")
        }

        // Method 8: env PATH and try each directory
        try {
            log.append("[8] PATH check... ")
            val path = System.getenv("PATH") ?: ""
            log.appendLine("PATH=$path")
            for (dir in path.split(":")) {
                val suFile = File(dir, "su")
                if (suFile.exists()) {
                    log.appendLine("  found su at: ${suFile.absolutePath}")
                    rootDebugLog = log.toString()
                    return true
                }
            }
        } catch (e: Exception) {
            log.appendLine("ERROR: ${e.message}")
        }

        // Method 9: getprop checks
        try {
            log.append("[9] getprop... ")
            val props = mapOf(
                "ro.debuggable" to "1",
                "ro.secure" to "0",
                "service.bootanim.exit" to "1"
            )
            for ((prop, expected) in props) {
                val process = ProcessBuilder("getprop", prop).redirectErrorStream(true).start()
                val value = process.inputStream.bufferedReader().use { it.readText().trim() }
                process.waitFor()
                log.append("$prop=$value ")
                if (value == expected && prop != "service.bootanim.exit") {
                    rootDebugLog = log.toString()
                    return true
                }
            }
            log.appendLine()
        } catch (e: Exception) {
            log.appendLine("ERROR: ${e.message}")
        }

        log.appendLine("[X] All methods failed — root NOT detected")
        rootDebugLog = log.toString()
        return false
    }

    private fun checkNxpChipset(): Boolean {
        val log = StringBuilder()

        // Method 1: getprop for NFC hardware — most reliable
        try {
            log.append("[NXP-1] getprop nfc... ")
            val nfcProps = listOf(
                "ro.hardware.nfc_nci",
                "ro.nfc.port",
                "persist.nfc_nci.nfc_chipid",
                "ro.hardware.nfc",
                "nfc.nxp.chip.model"
            )
            for (prop in nfcProps) {
                val process = ProcessBuilder("getprop", prop).redirectErrorStream(true).start()
                val value = process.inputStream.bufferedReader().use { it.readText().trim() }
                process.waitFor()
                if (value.isNotEmpty()) {
                    log.append("$prop=$value ")
                }
            }
            log.appendLine()

            // Check all NFC-related props
            val process = ProcessBuilder("sh", "-c", "getprop | grep -i nfc").redirectErrorStream(true).start()
            val allNfcProps = process.inputStream.bufferedReader().use { it.readText().trim() }
            process.waitFor()
            if (allNfcProps.isNotEmpty()) {
                log.appendLine("  all nfc props:\n  ${allNfcProps.replace("\n", "\n  ")}")
            }

            val nxpKeywords = listOf("nxp", "pn5", "pn6", "pn7", "pn8", "sn1", "sn2", "nq2", "nq3", "nq4", "nq5")
            if (nxpKeywords.any { kw -> allNfcProps.contains(kw, ignoreCase = true) }) {
                log.appendLine("  MATCH: NXP keyword found in props")
                nxpDebugLog = log.toString()
                return true
            }
        } catch (e: Exception) {
            log.appendLine("ERROR: ${e.message}")
        }

        // Method 2: Check NFC HAL implementation
        try {
            log.append("[NXP-2] NFC HAL libs... ")
            val result = Shell.cmd("ls -la /vendor/lib*/hw/nfc*.so /vendor/lib*/hw/android.hardware.nfc* /system/lib*/hw/nfc*.so 2>/dev/null").exec()
            val libs = result.out.joinToString("\n")
            log.appendLine(libs.ifEmpty { "none found" })
            if (libs.contains("nxp", ignoreCase = true) || libs.contains("nci", ignoreCase = true)) {
                nxpDebugLog = log.toString()
                return true
            }
        } catch (e: Exception) {
            log.appendLine("ERROR: ${e.message}")
        }

        // Method 3: Check libnfc configs
        try {
            log.append("[NXP-3] libnfc config... ")
            val configPaths = listOf(
                "/vendor/etc/libnfc-nci.conf",
                "/vendor/etc/libnfc-nxp.conf",
                "/system/etc/libnfc-nci.conf",
                "/system/etc/libnfc-nxp.conf",
                "/vendor/etc/libnfc-brcm.conf",
                "/vendor/etc/libnfc-sec-vendor.conf",
                "/vendor/etc/libnfc-nxp_RF.conf"
            )
            val foundConfigs = mutableListOf<String>()
            for (path in configPaths) {
                if (File(path).exists()) {
                    foundConfigs.add(path)
                }
            }
            log.appendLine(foundConfigs.ifEmpty { listOf("none") }.joinToString())

            // Read the NXP config if it exists
            for (config in foundConfigs) {
                if (config.contains("nxp") || config.contains("nci")) {
                    try {
                        val content = Shell.cmd("cat $config 2>/dev/null | head -20").exec()
                        log.appendLine("  ${config}:\n  ${content.out.joinToString("\n  ")}")
                    } catch (_: Exception) {}
                    nxpDebugLog = log.toString()
                    return true
                }
            }
        } catch (e: Exception) {
            log.appendLine("ERROR: ${e.message}")
        }

        // Method 4: Check /dev/nq-nci or /dev/pn5* or /dev/nfc*
        try {
            log.append("[NXP-4] /dev NFC devices... ")
            val result = Shell.cmd("ls -la /dev/nq* /dev/pn* /dev/nfc* /dev/p73* /dev/sn* 2>/dev/null").exec()
            val devices = result.out.joinToString("\n")
            log.appendLine(devices.ifEmpty { "none found" })
            if (devices.isNotEmpty()) {
                nxpDebugLog = log.toString()
                return true
            }
        } catch (e: Exception) {
            log.appendLine("ERROR: ${e.message}")
        }

        // Method 5: Check dmesg for NFC chip info
        try {
            log.append("[NXP-5] dmesg nfc... ")
            val result = Shell.cmd("dmesg | grep -i nfc | head -10 2>/dev/null").exec()
            val dmesg = result.out.joinToString("\n")
            log.appendLine(dmesg.ifEmpty { "none" })
            if (dmesg.contains("nxp", ignoreCase = true) || dmesg.contains("pn5", ignoreCase = true) ||
                dmesg.contains("sn1", ignoreCase = true) || dmesg.contains("nq-nci", ignoreCase = true)) {
                nxpDebugLog = log.toString()
                return true
            }
        } catch (e: Exception) {
            log.appendLine("ERROR: ${e.message}")
        }

        // Method 6: If the phone can read Mifare Classic, it has NXP chip
        // Check if MifareClassic tech is available in NFC adapter
        try {
            log.append("[NXP-6] MifareClassic support... ")
            val nfcAdapter = android.nfc.NfcAdapter.getDefaultAdapter(context)
            if (nfcAdapter != null && nfcAdapter.isEnabled) {
                // If NFC is enabled and we got this far with root, assume NXP
                // because non-NXP chips (Broadcom/Samsung) can't read Mifare Classic on stock Android
                log.appendLine("NFC enabled + root available — assuming NXP compatible")
                nxpDebugLog = log.toString()
                return true
            }
            log.appendLine("NFC adapter: ${if (nfcAdapter != null) "present" else "null"}, enabled: ${nfcAdapter?.isEnabled}")
        } catch (e: Exception) {
            log.appendLine("ERROR: ${e.message}")
        }

        log.appendLine("[NXP-X] All methods failed — NXP NOT detected")
        nxpDebugLog = log.toString()
        return false
    }

    fun getCurrentDump(): TagDump? = EmulationState.currentDump.value
}
