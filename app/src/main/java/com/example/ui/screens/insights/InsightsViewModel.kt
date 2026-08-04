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

data class InsightsUiState(
    val isLoading: Boolean = true,
    val memoryStreak: Int = 0,
    val countriesVisited: Int = 0,
    val citiesVisited: Int = 0,
    val distanceTraveled: Int = 0,
    val photosCaptured: Int = 0,
    val videosCaptured: Int = 0,
    val voiceNotesCaptured: Int = 0,
    val recentMediaCount: Int = 0,
    val topSentiment: String = "Unknown",
    val sentimentFractions: Map<String, Float> = emptyMap(),
    val monthlyActivity: List<Float> = List(12) { 0f },
    val yearlyActivity: List<Float> = List(5) { 0f },
    val favoriteLocations: List<String> = emptyList()
)

class InsightsViewModel(
    private val repository: MemoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(InsightsUiState())
    val uiState: StateFlow<InsightsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.allMemories.collect { memories ->
                calculateInsights(memories)
            }
        }
    }

    private fun calculateInsights(memories: List<MemoryWithMedia>) {
        if (memories.isEmpty()) {
            _uiState.value = InsightsUiState(isLoading = false)
            return
        }

        // Streak
        val streak = calculateStreak(memories)

        // Locations
        val locations = memories.mapNotNull { it.memory.locationName.takeIf { loc -> loc.isNotBlank() } }
        val distinctLocations = locations.distinct()
        val countries = distinctLocations.map { it.split(",").lastOrNull()?.trim() }.distinct().count { !it.isNullOrBlank() }
        val cities = distinctLocations.count()

        // Media
        var photos = 0
        var videos = 0
        var voiceNotes = 0
        var recentMediaCount = 0

        val oneMonthAgo = Calendar.getInstance().apply { add(Calendar.MONTH, -1) }.timeInMillis

        memories.forEach { memoryWithMedia ->
            memoryWithMedia.media.forEach { media ->
                when (media.type) {
                    "image" -> photos++
                    "video" -> videos++
                    "audio" -> voiceNotes++
                }
                
                if (memoryWithMedia.memory.timestamp > oneMonthAgo) {
                    if (media.type == "image" || media.type == "video" || media.type == "audio") {
                        recentMediaCount++
                    }
                }
            }
        }

        // Sentiment
        val sentiments = memories.mapNotNull { it.memory.sentiment.takeIf { s -> s.isNotBlank() } }
        val sentimentCounts = sentiments.groupingBy { it }.eachCount()
        val totalSentiments = sentimentCounts.values.sum()
        
        val sentimentFractions = if (totalSentiments > 0) {
            sentimentCounts.mapValues { it.value.toFloat() / totalSentiments }
        } else {
            emptyMap()
        }
        
        val topSentiment = sentimentCounts.maxByOrNull { it.value }?.key ?: "Unknown"

        // Favorite Locations
        val locationCounts = locations.groupingBy { it }.eachCount()
        val favoriteLocations = locationCounts.entries.sortedByDescending { it.value }.take(3).map { it.key }

        // Monthly / Yearly Activity
        val monthlyCounts = IntArray(12)
        val yearlyCounts = IntArray(5)
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)

        memories.forEach { 
            val cal = Calendar.getInstance()
            cal.timeInMillis = it.memory.timestamp
            
            // Monthly activity for current year
            if (cal.get(Calendar.YEAR) == currentYear) {
                monthlyCounts[cal.get(Calendar.MONTH)]++
            }
            
            // Yearly activity for last 5 years
            val yearDiff = currentYear - cal.get(Calendar.YEAR)
            if (yearDiff in 0..4) {
                yearlyCounts[4 - yearDiff]++
            }
        }

        val maxMonthly = monthlyCounts.maxOrNull()?.coerceAtLeast(1) ?: 1
        val maxYearly = yearlyCounts.maxOrNull()?.coerceAtLeast(1) ?: 1

        val monthlyActivity = monthlyCounts.map { it.toFloat() / maxMonthly }
        val yearlyActivity = yearlyCounts.map { it.toFloat() / maxYearly }

        _uiState.value = InsightsUiState(
            isLoading = false,
            memoryStreak = streak,
            countriesVisited = countries,
            citiesVisited = cities,
            distanceTraveled = 15300, // Static placeholder
            photosCaptured = photos,
            videosCaptured = videos,
            voiceNotesCaptured = voiceNotes,
            recentMediaCount = recentMediaCount,
            topSentiment = topSentiment,
            sentimentFractions = sentimentFractions,
            monthlyActivity = monthlyActivity,
            yearlyActivity = yearlyActivity,
            favoriteLocations = favoriteLocations
        )
    }

    private fun calculateStreak(memories: List<MemoryWithMedia>): Int {
        val sortedDates = memories.map {
            val cal = Calendar.getInstance()
            cal.timeInMillis = it.memory.timestamp
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            cal.timeInMillis
        }.distinct().sortedDescending()
        
        if (sortedDates.isEmpty()) return 0
        
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        
        val oneDay = 24 * 60 * 60 * 1000L
        
        var currentCheck = today
        var streak = 0
        
        if (sortedDates.first() == today || sortedDates.first() == today - oneDay) {
            currentCheck = sortedDates.first()
            for (date in sortedDates) {
                if (date == currentCheck) {
                    streak++
                    currentCheck -= oneDay
                } else {
                    break
                }
            }
        }
        
        return streak
    }
}
