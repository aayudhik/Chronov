package com.example.ui.screens.onthisday

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.MemoryWithMedia
import com.example.data.repository.OnThisDayRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import java.text.SimpleDateFormat

class OnThisDayViewModel(
    private val repository: OnThisDayRepository
) : ViewModel() {

    val settings = repository.getSettings().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        com.example.data.local.OnThisDaySettings()
    )

    private val _targetDate = MutableStateFlow(Date())
    val targetDate: StateFlow<Date> = _targetDate.asStateFlow()

    @kotlinx.coroutines.ExperimentalCoroutinesApi
    val memories: StateFlow<List<MemoryWithMedia>> = _targetDate
        .flatMapLatest { date -> repository.getMemoriesForDate(date) }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    private val _aiComparison = MutableStateFlow<String>("")
    val aiComparison: StateFlow<String> = _aiComparison.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val filteredMemories = combine(memories, _searchQuery) { mems, query ->
        if (query.isBlank()) mems
        else mems.filter { 
            it.memory.title.contains(query, ignoreCase = true) || 
            it.memory.aiSummary.contains(query, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val groupedMemories = filteredMemories.map { mems ->
        mems.groupBy { memoryWithMedia ->
            val memYear = SimpleDateFormat("yyyy", Locale.getDefault()).format(Date(memoryWithMedia.memory.timestamp)).toInt()
            val currentYear = SimpleDateFormat("yyyy", Locale.getDefault()).format(_targetDate.value).toInt()
            val diff = currentYear - memYear
            when (diff) {
                0 -> "This Year"
                1 -> "1 Year Ago"
                else -> "$diff Years Ago"
            }
        }.toSortedMap(compareBy { 
            when (it) {
                "This Year" -> 0
                "1 Year Ago" -> 1
                else -> it.split(" ").firstOrNull()?.toIntOrNull() ?: 999
            }
        })
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    init {
        viewModelScope.launch {
            memories.collect { mems ->
                if (mems.size >= 2) {
                    _aiComparison.value = repository.generateAiComparison(mems)
                } else {
                    _aiComparison.value = ""
                }
            }
        }
    }

    fun setTargetDate(date: Date) {
        _targetDate.value = date
    }
    
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateSettings(update: (com.example.data.local.OnThisDaySettings) -> com.example.data.local.OnThisDaySettings) {
        viewModelScope.launch {
            repository.saveSettings(update(settings.value))
        }
    }
}

class OnThisDayViewModelFactory(private val repository: OnThisDayRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OnThisDayViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return OnThisDayViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
