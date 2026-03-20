package com.nfcemulator.ui.emulator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nfcemulator.dump.model.DumpFormat
import com.nfcemulator.dump.model.TagDump
import com.nfcemulator.dump.model.TagType
import com.nfcemulator.nfc.emulator.EmulationState
import com.nfcemulator.nfc.hal.NfcEmulatorHal
import com.nfcemulator.nfc.writer.TagWriter
import com.nfcemulator.nfc.writer.WriteProgress
import com.nfcemulator.storage.EncryptedFileManager
import com.nfcemulator.ui.home.TagUiModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class EmulatorUiState(
    val selectedTag: TagUiModel? = null,
    val isEmulating: Boolean = false,
    val emulationMode: String = "HCE Standard (Limited)",
    val statusMessage: String = "",
    val writeProgress: String = ""
)

class EmulatorViewModel(
    private val nfcHal: NfcEmulatorHal,
    private val encryptedFileManager: EncryptedFileManager,
    private val tagWriter: TagWriter
) : ViewModel() {

    private val _uiState = MutableStateFlow(EmulatorUiState())
    val uiState: StateFlow<EmulatorUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val capabilities = nfcHal.getCapabilities()
            _uiState.update { it.copy(emulationMode = capabilities.emulationMode.displayName) }
        }
        // Observe EmulationState
        viewModelScope.launch {
            EmulationState.isEmulating.collect { emulating ->
                _uiState.update { it.copy(isEmulating = emulating) }
            }
        }
        // Observe write progress
        viewModelScope.launch {
            tagWriter.progress.collect { progress ->
                val message = when (progress) {
                    is WriteProgress.Idle -> ""
                    is WriteProgress.WaitingForTag -> "Waiting for magic tag... Hold a blank tag near the phone"
                    is WriteProgress.Writing -> "Writing sector ${progress.sector}/${progress.total}..."
                    is WriteProgress.Complete -> "Write complete! ${progress.sectorsWritten}/${progress.totalSectors} sectors written"
                    is WriteProgress.Error -> progress.message
                }
                _uiState.update { it.copy(writeProgress = message) }
            }
        }
    }

    fun selectTag(tag: TagUiModel) {
        _uiState.update { it.copy(selectedTag = tag, statusMessage = "") }
    }

    fun startEmulation() {
        val tag = _uiState.value.selectedTag ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(statusMessage = "Starting emulation...") }

            val rawData = encryptedFileManager.loadDump(tag.id)
            if (rawData == null) {
                _uiState.update { it.copy(statusMessage = "Error: dump data not found") }
                return@launch
            }

            val dump = TagDump(
                id = tag.id,
                name = tag.name,
                type = TagType.entries.find { it.displayName == tag.type } ?: TagType.UNKNOWN,
                uid = parseUidHex(tag.uid),
                rawData = rawData,
                sourceFormat = DumpFormat.JSON
            )

            val result = nfcHal.startEmulation(dump)
            val message = when (result) {
                is com.nfcemulator.dump.model.EmulationResult.Started ->
                    "Emulation active — hold phone near reader"
                is com.nfcemulator.dump.model.EmulationResult.Error ->
                    "Error: ${result.message}"
                is com.nfcemulator.dump.model.EmulationResult.UnsupportedType ->
                    "Unsupported: ${result.tagType.displayName} requires ${result.requiredMode.displayName}"
                is com.nfcemulator.dump.model.EmulationResult.Stopped -> ""
            }
            _uiState.update { it.copy(statusMessage = message) }
        }
    }

    fun startWriteMode() {
        tagWriter.startWaiting()
    }

    fun stopEmulation() {
        viewModelScope.launch {
            nfcHal.stopEmulation()
            _uiState.update { it.copy(statusMessage = "Emulation stopped") }
        }
    }

    private fun parseUidHex(uid: String): ByteArray {
        return try {
            uid.split(":").map { it.toInt(16).toByte() }.toByteArray()
        } catch (_: Exception) {
            byteArrayOf()
        }
    }
}
