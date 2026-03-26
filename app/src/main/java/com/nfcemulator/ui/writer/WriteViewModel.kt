package com.nfcemulator.ui.writer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nfcemulator.dump.model.DumpFormat
import com.nfcemulator.dump.model.TagDump
import com.nfcemulator.dump.model.TagType
import com.nfcemulator.dump.parser.MfdParser
import com.nfcemulator.nfc.writer.TagWriter
import com.nfcemulator.nfc.writer.WriteProgress
import com.nfcemulator.storage.EncryptedFileManager
import com.nfcemulator.storage.local.TagDao
import com.nfcemulator.ui.home.TagUiModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class WriteUiState(
    val selectedTag: TagUiModel? = null,
    val writeProgress: String = "",
    val tags: List<TagUiModel> = emptyList()
)

class WriteViewModel(
    private val tagDao: TagDao,
    private val tagWriter: TagWriter,
    private val encryptedFileManager: EncryptedFileManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(WriteUiState())
    val uiState: StateFlow<WriteUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            tagDao.getAllTags().collect { entities ->
                val models = entities.map { entity ->
                    TagUiModel(
                        id = entity.id,
                        name = entity.name,
                        uid = entity.uid,
                        type = entity.type,
                        category = entity.category,
                        keysFound = entity.keysFound,
                        keysTotal = entity.keysTotal,
                        lastEmulated = entity.lastEmulatedAt?.let { "Emulated" }
                    )
                }
                _uiState.update { it.copy(tags = models) }
            }
        }
        viewModelScope.launch {
            tagWriter.progress.collect { progress ->
                val message = when (progress) {
                    is WriteProgress.Idle -> ""
                    is WriteProgress.WaitingForTag -> "Waiting for magic tag… Hold a blank tag near the phone"
                    is WriteProgress.Writing -> "Writing sector ${progress.sector}/${progress.total}…"
                    is WriteProgress.Complete -> "Write complete! ${progress.sectorsWritten}/${progress.totalSectors} sectors written"
                    is WriteProgress.Error -> progress.message
                }
                _uiState.update { it.copy(writeProgress = message) }
            }
        }
    }

    fun selectTag(tag: TagUiModel) {
        _uiState.update { it.copy(selectedTag = tag, writeProgress = "") }
    }

    fun startWrite() {
        val tag = _uiState.value.selectedTag ?: return
        viewModelScope.launch {
            val rawData = encryptedFileManager.loadDump(tag.id)
            if (rawData == null) {
                _uiState.update { it.copy(writeProgress = "Error: dump data not found") }
                return@launch
            }
            val parser = MfdParser()
            val parsedDump = try {
                parser.parse(java.io.ByteArrayInputStream(rawData), "${tag.name}.bin")
            } catch (_: Exception) { null }

            val dump = if (parsedDump != null && parsedDump.sectors.isNotEmpty()) {
                parsedDump.copy(
                    id = tag.id,
                    name = tag.name,
                    uid = try {
                        tag.uid.split(":").map { it.toInt(16).toByte() }.toByteArray()
                    } catch (_: Exception) { parsedDump.uid }
                )
            } else {
                TagDump(
                    id = tag.id,
                    name = tag.name,
                    type = TagType.entries.find { it.displayName == tag.type } ?: TagType.MIFARE_CLASSIC_1K,
                    uid = try {
                        tag.uid.split(":").map { it.toInt(16).toByte() }.toByteArray()
                    } catch (_: Exception) { byteArrayOf() },
                    rawData = rawData,
                    sourceFormat = DumpFormat.BIN
                )
            }
            tagWriter.startWaiting(dump)
        }
    }

    fun cancelWrite() {
        tagWriter.reset()
    }
}
