package com.example.ui.screens.stories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.Story
import com.example.data.repository.MemoryRepository
import com.example.domain.StoryGenerationService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class StoryUiState(
    val stories: List<Story> = emptyList(),
    val isGenerating: Boolean = false,
    val generationError: String? = null
)

class StoryViewModel(
    private val memoryRepository: MemoryRepository,
    private val storyGenerationService: StoryGenerationService
) : ViewModel() {

    private val _isGenerating = MutableStateFlow(false)
    private val _generationError = MutableStateFlow<String?>(null)

    val uiState: StateFlow<StoryUiState> = kotlinx.coroutines.flow.combine(
        memoryRepository.allStories,
        _isGenerating,
        _generationError
    ) { stories, isGenerating, error ->
        StoryUiState(stories, isGenerating, error)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StoryUiState())

    fun generateStory(type: String, title: String) {
        viewModelScope.launch {
            _isGenerating.value = true
            _generationError.value = null
            
            val calendar = Calendar.getInstance()
            val endTime = calendar.timeInMillis
            
            when (type) {
                "Daily recap" -> calendar.add(Calendar.DAY_OF_YEAR, -1)
                "Weekly recap" -> calendar.add(Calendar.DAY_OF_YEAR, -7)
                "Monthly recap" -> calendar.add(Calendar.MONTH, -1)
                "Year in review" -> calendar.add(Calendar.YEAR, -1)
                else -> calendar.add(Calendar.MONTH, -1) // Default for travel, festival, birthday etc
            }
            val startTime = calendar.timeInMillis
            
            val memories = memoryRepository.allMemories.first().filter { it.memory.timestamp in startTime..endTime }
            
            if (memories.isEmpty()) {
                _generationError.value = "Not enough memories in this time period."
                _isGenerating.value = false
                return@launch
            }

            val story = storyGenerationService.generateStory(memories, type, title, startTime, endTime)
            
            if (story != null) {
                memoryRepository.insertStory(story)
            } else {
                _generationError.value = "Failed to generate story."
            }
            
            _isGenerating.value = false
        }
    }

    fun deleteStory(id: Long) {
        viewModelScope.launch {
            memoryRepository.deleteStory(id)
        }
    }
}
