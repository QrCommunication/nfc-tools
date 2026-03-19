package com.nfcemulator.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nfcemulator.storage.local.TagDao
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HomeViewModel(private val tagDao: TagDao) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")

    val tags: StateFlow<List<TagUiModel>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) tagDao.getAllTags()
            else tagDao.searchTags(query)
        }
        .map { entities ->
            entities.map { entity ->
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
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun search(query: String) {
        _searchQuery.value = query
    }

    fun deleteTag(id: String) {
        viewModelScope.launch {
            tagDao.deleteTagById(id)
        }
    }

    fun renameTag(id: String, newName: String) {
        viewModelScope.launch {
            val tag = tagDao.getTagById(id) ?: return@launch
            tagDao.updateTag(tag.copy(name = newName))
        }
    }
}
