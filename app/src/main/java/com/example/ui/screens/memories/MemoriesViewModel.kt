package com.example.ui.screens.memories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.MemoryWithMedia
import com.example.data.repository.MemoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class MemoriesUiState(
    val memories: List<MemoryWithMedia> = emptyList(),
    val availableTags: List<String> = emptyList(),
    val selectedTag: String? = null
)

class MemoriesViewModel(private val memoryRepository: MemoryRepository) : ViewModel() {
    
    private val selectedTag = MutableStateFlow<String?>(null)

    val uiState: StateFlow<MemoriesUiState> = combine(
        memoryRepository.allMemories,
        selectedTag
    ) { allMemories, selected ->
        val memoriesWithImages = allMemories.filter { memoryWithMedia ->
            memoryWithMedia.media.any { it.type == "image" }
        }

        val availableTags = allMemories
            .flatMap { it.media }
            .filter { it.type == "tag" }
            .map { it.label }
            .distinct()
            .sorted()

        val filteredMemories = if (selected == null) {
            memoriesWithImages
        } else {
            memoriesWithImages.filter { memoryWithMedia ->
                memoryWithMedia.media.any { it.type == "tag" && it.label == selected }
            }
        }

        MemoriesUiState(
            memories = filteredMemories,
            availableTags = availableTags,
            selectedTag = selected
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MemoriesUiState()
    )

    fun selectTag(tag: String?) {
        selectedTag.value = tag
    }
}
