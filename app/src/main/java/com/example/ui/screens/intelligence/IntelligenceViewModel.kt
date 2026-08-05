package com.example.ui.screens.intelligence

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.MemoryRepository
import com.example.domain.MemoryIntelligenceService
import com.example.domain.MemoryScores
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class SortOption {
    OVERALL, IMPORTANCE, EMOTIONAL, HAPPINESS, CONFIDENCE, ACTIVITY, SOCIAL, TRAVEL, PHOTOGRAPHY
}

data class IntelligenceUiState(
    val memoryScores: List<MemoryScores> = emptyList(),
    val top100: List<MemoryScores> = emptyList(),
    val isLoading: Boolean = true,
    val currentSort: SortOption = SortOption.OVERALL
)

class IntelligenceViewModel(
    private val memoryRepository: MemoryRepository,
    private val intelligenceService: MemoryIntelligenceService
) : ViewModel() {

    private val _currentSort = MutableStateFlow(SortOption.OVERALL)
    private val _memoryScores = MutableStateFlow<List<MemoryScores>>(emptyList())
    private val _isLoading = MutableStateFlow(true)

    val uiState: StateFlow<IntelligenceUiState> = combine(
        _memoryScores,
        _currentSort,
        _isLoading
    ) { scores, sort, loading ->
        val sorted = when (sort) {
            SortOption.OVERALL -> scores.sortedByDescending { it.overallScore }
            SortOption.IMPORTANCE -> scores.sortedByDescending { it.importanceScore }
            SortOption.EMOTIONAL -> scores.sortedByDescending { it.emotionalScore }
            SortOption.HAPPINESS -> scores.sortedByDescending { it.happinessScore }
            SortOption.CONFIDENCE -> scores.sortedByDescending { it.confidenceScore }
            SortOption.ACTIVITY -> scores.sortedByDescending { it.activityScore }
            SortOption.SOCIAL -> scores.sortedByDescending { it.socialScore }
            SortOption.TRAVEL -> scores.sortedByDescending { it.travelScore }
            SortOption.PHOTOGRAPHY -> scores.sortedByDescending { it.photographyScore }
        }
        IntelligenceUiState(
            memoryScores = sorted,
            top100 = sorted.take(100),
            isLoading = loading,
            currentSort = sort
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), IntelligenceUiState())

    init {
        loadIntelligence()
    }

    private fun loadIntelligence() {
        viewModelScope.launch {
            _isLoading.value = true
            val memories = memoryRepository.allMemories.first()
            val scores = intelligenceService.analyzeMemories(memories)
            _memoryScores.value = scores
            _isLoading.value = false
        }
    }

    fun setSortOption(option: SortOption) {
        _currentSort.value = option
    }
}
