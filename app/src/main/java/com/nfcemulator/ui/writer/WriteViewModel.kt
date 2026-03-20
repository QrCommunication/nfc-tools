package com.nfcemulator.ui.writer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nfcemulator.nfc.writer.TagWriter
import com.nfcemulator.nfc.writer.WriteProgress
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
    private val tagWriter: TagWriter
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
        tagWriter.startWaiting()
    }

    fun cancelWrite() {
        tagWriter.reset()
    }
}
