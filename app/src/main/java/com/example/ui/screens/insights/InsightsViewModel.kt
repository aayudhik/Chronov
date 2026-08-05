package com.example.ui.screens.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.MemoryWithMedia
import com.example.data.repository.MemoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Locale

enum class DateRangeFilter {
    ALL_TIME, THIS_YEAR, LAST_YEAR, LAST_6_MONTHS
}

data class AdvancedInsightsUiState(
    val isLoading: Boolean = true,
    val selectedFilter: DateRangeFilter = DateRangeFilter.ALL_TIME,
    
    // Overview
    val totalMemories: Int = 0,
    val photosCaptured: Int = 0,
    val videosCaptured: Int = 0,
    val voiceNotesCaptured: Int = 0,
    
    // Activity & Heatmap
    val mostActiveMonth: String = "Unknown",
    val favoriteWeekday: String = "Unknown",
    val heatmapData: Map<Long, Int> = emptyMap(), // Start of day timestamp -> count
    
    // Travel & Map
    val longestTripDays: Int = 0,
    val countriesVisited: Int = 0,
    val citiesVisited: Int = 0,
    val distanceTraveledMiles: Int = 0,
    val mostVisitedPlaces: List<String> = emptyList(),
    
    // People
    val mostPhotographedPeople: List<String> = emptyList(),
    
    // Mood & Expenses
    val moodTrends: Map<String, Float> = emptyMap(),
    val expenseTrends: List<Float> = emptyList(),
    
    // AI
    val aiYearlyInsight: String = "Analyzing your journey..."
)

class InsightsViewModel(
    private val repository: MemoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdvancedInsightsUiState())
    val uiState: StateFlow<AdvancedInsightsUiState> = _uiState.asStateFlow()
    
    private var allMemories: List<MemoryWithMedia> = emptyList()

    init {
        viewModelScope.launch {
            repository.allMemories.collect { memories ->
                allMemories = memories
                calculateInsights()
            }
        }
    }
    
    fun setDateFilter(filter: DateRangeFilter) {
        _uiState.value = _uiState.value.copy(selectedFilter = filter, isLoading = true)
        calculateInsights()
    }

    private fun calculateInsights() {
        val filter = _uiState.value.selectedFilter
        val filteredMemories = filterMemories(allMemories, filter)
        
        if (filteredMemories.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                totalMemories = 0,
                aiYearlyInsight = "No memories found for this period."
            )
            return
        }

        // Media Counts
        var photos = 0
        var videos = 0
        var voiceNotes = 0
        val peopleCounts = mutableMapOf<String, Int>()
        
        filteredMemories.forEach { mwm ->
            mwm.media.forEach { media ->
                when (media.type) {
                    "image" -> photos++
                    "video" -> videos++
                    "audio" -> voiceNotes++
                    "tag" -> {
                        val tag = media.label.trim()
                        if (tag.isNotBlank()) {
                            peopleCounts[tag] = (peopleCounts[tag] ?: 0) + 1
                        }
                    }
                }
            }
        }
        val topPeople = peopleCounts.entries.sortedByDescending { it.value }.take(5).map { it.key }

        // Locations
        val locations = filteredMemories.mapNotNull { it.memory.locationName.takeIf { loc -> loc.isNotBlank() } }
        val locationCounts = locations.groupingBy { it }.eachCount()
        val mostVisitedPlaces = locationCounts.entries.sortedByDescending { it.value }.take(5).map { it.key }
        
        val distinctLocations = locations.distinct()
        val countries = distinctLocations.map { it.split(",").lastOrNull()?.trim() }.distinct().count { !it.isNullOrBlank() }
        val cities = distinctLocations.count()
        val distance = filteredMemories.size * 12 // Simulated distance
        val longestTrip = (filteredMemories.size / 5).coerceAtLeast(1)

        // Activity
        val monthCounts = mutableMapOf<Int, Int>()
        val weekdayCounts = mutableMapOf<Int, Int>()
        val heatmap = mutableMapOf<Long, Int>()
        
        filteredMemories.forEach { 
            val cal = Calendar.getInstance().apply { timeInMillis = it.memory.timestamp }
            val month = cal.get(Calendar.MONTH)
            val weekday = cal.get(Calendar.DAY_OF_WEEK)
            
            monthCounts[month] = (monthCounts[month] ?: 0) + 1
            weekdayCounts[weekday] = (weekdayCounts[weekday] ?: 0) + 1
            
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val dayStart = cal.timeInMillis
            heatmap[dayStart] = (heatmap[dayStart] ?: 0) + 1
        }
        
        val topMonthIndex = monthCounts.maxByOrNull { it.value }?.key ?: 0
        val monthsStr = arrayOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
        val mostActiveMonth = monthsStr[topMonthIndex]
        
        val topWeekdayIndex = weekdayCounts.maxByOrNull { it.value }?.key ?: 1
        val weekdaysStr = arrayOf("", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
        val favoriteWeekday = weekdaysStr[topWeekdayIndex]

        // Mood Trends
        val sentiments = filteredMemories.mapNotNull { it.memory.sentiment.takeIf { s -> s.isNotBlank() } }
        val sentimentCounts = sentiments.groupingBy { it }.eachCount()
        val totalSentiments = sentimentCounts.values.sum().coerceAtLeast(1)
        val moodTrends = sentimentCounts.mapValues { it.value.toFloat() / totalSentiments }

        // Expenses (Simulated)
        val expenseTrends = List(12) { index -> 
            if (monthCounts.containsKey(index)) (monthCounts[index] ?: 0).toFloat() * 15.5f else (Math.random() * 50).toFloat()
        }

        // AI Insight
        val aiInsight = "During this period, you were highly active in $mostActiveMonth and preferred $favoriteWeekday. You explored $cities cities across $countries countries, covering approximately $distance miles. Your prominent mood was ${moodTrends.maxByOrNull { it.value }?.key ?: "Neutral"}."

        _uiState.value = AdvancedInsightsUiState(
            isLoading = false,
            selectedFilter = filter,
            totalMemories = filteredMemories.size,
            photosCaptured = photos,
            videosCaptured = videos,
            voiceNotesCaptured = voiceNotes,
            mostActiveMonth = mostActiveMonth,
            favoriteWeekday = favoriteWeekday,
            heatmapData = heatmap,
            longestTripDays = longestTrip,
            countriesVisited = countries,
            citiesVisited = cities,
            distanceTraveledMiles = distance,
            mostVisitedPlaces = mostVisitedPlaces,
            mostPhotographedPeople = topPeople,
            moodTrends = moodTrends,
            expenseTrends = expenseTrends,
            aiYearlyInsight = aiInsight
        )
    }
    
    private fun filterMemories(memories: List<MemoryWithMedia>, filter: DateRangeFilter): List<MemoryWithMedia> {
        val cal = Calendar.getInstance()
        return when (filter) {
            DateRangeFilter.ALL_TIME -> memories
            DateRangeFilter.THIS_YEAR -> {
                val currentYear = cal.get(Calendar.YEAR)
                memories.filter { 
                    val mCal = Calendar.getInstance().apply { timeInMillis = it.memory.timestamp }
                    mCal.get(Calendar.YEAR) == currentYear
                }
            }
            DateRangeFilter.LAST_YEAR -> {
                val lastYear = cal.get(Calendar.YEAR) - 1
                memories.filter { 
                    val mCal = Calendar.getInstance().apply { timeInMillis = it.memory.timestamp }
                    mCal.get(Calendar.YEAR) == lastYear
                }
            }
            DateRangeFilter.LAST_6_MONTHS -> {
                cal.add(Calendar.MONTH, -6)
                val cutoff = cal.timeInMillis
                memories.filter { it.memory.timestamp >= cutoff }
            }
        }
    }
}
