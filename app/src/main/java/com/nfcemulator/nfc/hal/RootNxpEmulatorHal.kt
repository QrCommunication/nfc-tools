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
    var rootDebugLog: String = ""
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
