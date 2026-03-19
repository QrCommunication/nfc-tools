package com.nfcemulator.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nfcemulator.dump.analyzer.DictionaryManager
import com.nfcemulator.nfc.hal.NfcEmulatorHal
import com.nfcemulator.nfc.hal.RootNxpEmulatorHal
import com.nfcemulator.storage.EncryptedFileManager
import com.nfcemulator.storage.local.TagDao
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SettingsUiState(
    val hasRoot: Boolean = false,
    val hasNxpChipset: Boolean = false,
    val emulationMode: String = "Checking...",
    val totalKeys: Int = 0,
    val totalTags: Int = 0,
    val storageSize: String = "0 KB",
    val nxpDebugLog: String = ""
)

class SettingsViewModel(
    private val nfcHal: NfcEmulatorHal,
    private val dictionaryManager: DictionaryManager,
    private val tagDao: TagDao,
    private val encryptedFileManager: EncryptedFileManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadStats()
    }

    fun loadStats() {
        viewModelScope.launch {
            // Invalidate cache to force fresh root/NXP check
            (nfcHal as? RootNxpEmulatorHal)?.invalidateCache()
            val capabilities = nfcHal.getCapabilities()
            val tagCount = tagDao.getTagCount()
            val storageBytes = encryptedFileManager.getDumpsSize()
            val totalKeys = dictionaryManager.getTotalKeyCount()

            val nxpLog = (nfcHal as? RootNxpEmulatorHal)?.nxpDebugLog ?: ""

            _uiState.value = SettingsUiState(
                hasRoot = capabilities.hasRoot,
                hasNxpChipset = capabilities.hasNxpChipset,
                emulationMode = capabilities.emulationMode.displayName,
                totalKeys = if (totalKeys > 0) totalKeys else dictionaryManager.getDefaultKeys().size,
                totalTags = tagCount,
                storageSize = formatSize(storageBytes),
                nxpDebugLog = nxpLog
            )
        }
    }

    private fun formatSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            else -> "${"%.1f".format(bytes / (1024.0 * 1024.0))} MB"
        }
    }
}
