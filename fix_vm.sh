cat << 'INNER_EOF' > app/src/main/java/com/example/ui/screens/map/MapViewModel.kt
package com.example.ui.screens.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.MemoryWithMedia
import com.example.data.repository.MemoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class MapCluster(
    val latitude: Double,
    val longitude: Double,
    val memories: List<MemoryWithMedia>
)

data class MapUiState(
    val memories: List<MemoryWithMedia> = emptyList(),
    val filteredMemories: List<MemoryWithMedia> = emptyList(),
    val selectedYear: String? = null,
    val selectedPerson: String? = null,
    val selectedCollection: String? = null,
    val selectedMood: String? = null,
    val selectedActivity: String? = null,
    val availableYears: List<String> = emptyList(),
    val availablePersons: List<String> = emptyList(),
    val availableCollections: List<String> = emptyList(),
    val availableMoods: List<String> = emptyList(),
    val availableActivities: List<String> = emptyList(),
    val clusters: List<MapCluster> = emptyList()
)

class MapViewModel(private val repository: MemoryRepository) : ViewModel() {
    private val selectedYear = MutableStateFlow<String?>(null)
    private val selectedPerson = MutableStateFlow<String?>(null)
    private val selectedCollection = MutableStateFlow<String?>(null)
    private val selectedMood = MutableStateFlow<String?>(null)
    private val selectedActivity = MutableStateFlow<String?>(null)

    val uiState: StateFlow<MapUiState> = combine(
        repository.allMemories,
        combine(selectedYear, selectedPerson, selectedCollection, selectedMood, selectedActivity) { y, p, c, m, a -> 
            FilterState(y, p, c, m, a) 
        }
    ) { all, filters ->
        val year = filters.year
        val person = filters.person
        val collection = filters.collection
        val mood = filters.mood
        val activity = filters.activity

        val withLocation = all.filter { it.memory.latitude != null && it.memory.longitude != null }
        
        val years = withLocation.map { 
            SimpleDateFormat("yyyy", Locale.getDefault()).format(Date(it.memory.timestamp))
        }.distinct().sorted()

        val persons = withLocation.flatMap { it.media }.filter { it.type == "tag" }.map { it.label }.distinct().sorted()
        val collections = withLocation.map { it.memory.aiCategory }.filter { it.isNotBlank() }.distinct().sorted()
        val moods = withLocation.map { it.memory.sentiment }.filter { it.isNotBlank() }.distinct().sorted()
        val activities = withLocation.map { it.memory.aiActivityDetection }.filter { it.isNotBlank() }.distinct().sorted()

        var filtered = withLocation
        if (year != null) {
            filtered = filtered.filter { 
                SimpleDateFormat("yyyy", Locale.getDefault()).format(Date(it.memory.timestamp)) == year 
            }
        }
        if (person != null) {
            filtered = filtered.filter { mem -> mem.media.any { it.type == "tag" && it.label == person } }
        }
        if (collection != null) {
            filtered = filtered.filter { it.memory.aiCategory == collection }
        }
        if (mood != null) {
            filtered = filtered.filter { it.memory.sentiment == mood }
        }
        if (activity != null) {
            filtered = filtered.filter { it.memory.aiActivityDetection == activity }
        }

        MapUiState(
            memories = withLocation,
            filteredMemories = filtered,
            selectedYear = year,
            selectedPerson = person,
            selectedCollection = collection,
            selectedMood = mood,
            selectedActivity = activity,
            availableYears = years,
            availablePersons = persons,
            availableCollections = collections,
            availableMoods = moods,
            availableActivities = activities,
            clusters = clusterMemories(filtered)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MapUiState())

    fun setYear(v: String?) { selectedYear.value = v }
    fun setPerson(v: String?) { selectedPerson.value = v }
    fun setCollection(v: String?) { selectedCollection.value = v }
    fun setMood(v: String?) { selectedMood.value = v }
    fun setActivity(v: String?) { selectedActivity.value = v }
}

data class FilterState(
    val year: String?,
    val person: String?,
    val collection: String?,
    val mood: String?,
    val activity: String?
)

private fun clusterMemories(memories: List<MemoryWithMedia>): List<MapCluster> {
    val clusters = mutableListOf<MapCluster>()
    val threshold = 1.0 // Degrees
    for (mem in memories) {
        val lat = mem.memory.latitude ?: continue
        val lon = mem.memory.longitude ?: continue
        var added = false
        for (i in clusters.indices) {
            val cluster = clusters[i]
            val dLat = cluster.latitude - lat
            val dLon = cluster.longitude - lon
            if (dLat * dLat + dLon * dLon < threshold * threshold) {
                val newMemories = cluster.memories + mem
                val avgLat = newMemories.map { it.memory.latitude!! }.average()
                val avgLon = newMemories.map { it.memory.longitude!! }.average()
                clusters[i] = MapCluster(avgLat, avgLon, newMemories)
                added = true
                break
            }
        }
        if (!added) {
            clusters.add(MapCluster(lat, lon, listOf(mem)))
        }
    }
    return clusters
}
INNER_EOF
