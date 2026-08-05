package com.example.ui.screens.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.MemoryWithMedia
import com.example.data.repository.MemoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import com.example.data.ai.AIMemoryEngine

sealed class MemoryDetailsUiState {
    object Loading : MemoryDetailsUiState()
    data class Success(val memoryWithMedia: MemoryWithMedia) : MemoryDetailsUiState()
    data class Error(val message: String) : MemoryDetailsUiState()
}

class MemoryDetailsViewModel(
    private val repository: MemoryRepository,
    private val aiMemoryEngine: AIMemoryEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow<MemoryDetailsUiState>(MemoryDetailsUiState.Loading)
    val uiState: StateFlow<MemoryDetailsUiState> = _uiState.asStateFlow()
    
    private val _relatedMemories = MutableStateFlow<List<MemoryWithMedia>>(emptyList())
    val relatedMemories: StateFlow<List<MemoryWithMedia>> = _relatedMemories.asStateFlow()
    
    private val _isEditing = MutableStateFlow(false)
    val isEditing: StateFlow<Boolean> = _isEditing.asStateFlow()

    private var currentMemoryId: Long = -1

    fun setEditing(editing: Boolean) {
        _isEditing.value = editing
    }

    fun updateMemoryDetails(title: String, locationName: String, notes: String, sentiment: String, score: String) {
        val currentState = _uiState.value
        if (currentState is MemoryDetailsUiState.Success) {
            val memory = currentState.memoryWithMedia.memory
            val newScore = score.toIntOrNull() ?: memory.score
            val updatedMemory = memory.copy(
                title = title,
                locationName = locationName,
                notes = notes,
                sentiment = sentiment,
                score = newScore
            )
            viewModelScope.launch {
                repository.updateMemory(updatedMemory)
                _isEditing.value = false
                aiMemoryEngine.analyzeMemory(currentMemoryId)
            }
        }
    }

    fun regenerateAiAnalysis() {
        if (currentMemoryId != -1L) {
            viewModelScope.launch {
                aiMemoryEngine.analyzeMemory(currentMemoryId)
            }
        }
    }

    fun loadMemory(memoryId: Long) {
        currentMemoryId = memoryId
        
        viewModelScope.launch {
            _uiState.value = MemoryDetailsUiState.Loading
            repository.getMemoryWithMedia(memoryId)
                .catch { e ->
                    _uiState.value = MemoryDetailsUiState.Error(e.message ?: "Unknown error")
                }
                .collect { memory ->
                    if (memory != null) {
                        _uiState.value = MemoryDetailsUiState.Success(memory)
                        
                        // Load related memories based on AI category
                        viewModelScope.launch {
                            val list = repository.allMemories.firstOrNull() ?: emptyList()
                            _relatedMemories.value = list.filter {
                                it.memory.id != memoryId && 
                                it.memory.aiCategory.isNotEmpty() && 
                                it.memory.aiCategory == memory.memory.aiCategory
                            }.take(3).ifEmpty {
                                list.filter { it.memory.id != memoryId }.take(3)
                            }
                        }
                    } else {
                        _uiState.value = MemoryDetailsUiState.Error("Memory not found")
                    }
                }
        }
    }
    
    fun toggleFavorite() {
        val currentState = _uiState.value
        if (currentState is MemoryDetailsUiState.Success) {
            val memory = currentState.memoryWithMedia.memory
            viewModelScope.launch {
                repository.updateMemory(memory.copy(isHero = !memory.isHero))
            }
        }
    }
    
    fun deleteMemory(onDeleted: () -> Unit) {
        val currentState = _uiState.value
        if (currentState is MemoryDetailsUiState.Success) {
            viewModelScope.launch {
                repository.deleteMemory(currentState.memoryWithMedia.memory)
                onDeleted()
            }
        }
    }
}
