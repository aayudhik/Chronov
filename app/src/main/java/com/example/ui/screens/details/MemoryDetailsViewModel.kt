package com.example.ui.screens.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.MemoryWithMedia
import com.example.data.repository.MemoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

sealed class MemoryDetailsUiState {
    object Loading : MemoryDetailsUiState()
    data class Success(val memoryWithMedia: MemoryWithMedia) : MemoryDetailsUiState()
    data class Error(val message: String) : MemoryDetailsUiState()
}

class MemoryDetailsViewModel(
    private val repository: MemoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<MemoryDetailsUiState>(MemoryDetailsUiState.Loading)
    val uiState: StateFlow<MemoryDetailsUiState> = _uiState.asStateFlow()
    
    private val _relatedMemories = MutableStateFlow<List<MemoryWithMedia>>(emptyList())
    val relatedMemories: StateFlow<List<MemoryWithMedia>> = _relatedMemories.asStateFlow()
    
    private var currentMemoryId: Long = -1

    fun loadMemory(memoryId: Long) {
        currentMemoryId = memoryId
        
        viewModelScope.launch {
            repository.allMemories.collect { list ->
                _relatedMemories.value = list.filter { it.memory.id != memoryId }.take(3)
            }
        }
        
        viewModelScope.launch {
            _uiState.value = MemoryDetailsUiState.Loading
            repository.getMemoryWithMedia(memoryId)
                .catch { e ->
                    _uiState.value = MemoryDetailsUiState.Error(e.message ?: "Unknown error")
                }
                .collect { memory ->
                    if (memory != null) {
                        _uiState.value = MemoryDetailsUiState.Success(memory)
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
