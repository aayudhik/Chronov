package com.example.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.auth.AuthRepository
import com.example.data.repository.MemoryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.TimeZone

data class ProfileUiState(
    val totalMemories: Int = 0,
    val distinctLocations: Int = 0,
    val totalImages: Int = 0,
    val longestStreak: Int = 0,
    val userEmailOrPhone: String? = null,
    val memories: List<com.example.data.local.MemoryWithMedia> = emptyList()
)

class ProfileViewModel(
    private val memoryRepository: MemoryRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    
    val uiState: StateFlow<ProfileUiState> = kotlinx.coroutines.flow.combine(
        memoryRepository.allMemories,
        authRepository.currentUser
    ) { memories, user ->
        val totalMemories = memories.size
        val distinctLocations = memories.map { it.memory.locationName }.filter { it.isNotBlank() }.distinct().size
        val totalImages = memories.flatMap { it.media }.count { it.type == "image" }

        val timestamps = memories.map { it.memory.timestamp }.sorted()
        var currentStreak = 0
        var maxStreak = 0
        var previousDayStart = 0L
        
        for (timestamp in timestamps) {
            val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { 
                timeInMillis = timestamp
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val dayStart = cal.timeInMillis
            
            if (previousDayStart == 0L) {
                currentStreak = 1
                maxStreak = 1
            } else {
                val diffMillis = dayStart - previousDayStart
                val diffDays = (diffMillis / (1000 * 60 * 60 * 24)).toInt()
                if (diffDays == 1) {
                    currentStreak++
                    if (currentStreak > maxStreak) maxStreak = currentStreak
                } else if (diffDays > 1) {
                    currentStreak = 1
                }
            }
            previousDayStart = dayStart
        }

        ProfileUiState(
            totalMemories = totalMemories,
            distinctLocations = distinctLocations,
            totalImages = totalImages,
            longestStreak = maxStreak,
            userEmailOrPhone = user?.email ?: user?.phoneNumber,
            memories = memories
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProfileUiState()
    )

    fun generateExportText(
        memories: List<com.example.data.local.MemoryWithMedia>,
        format: String,
        includeLocations: Boolean,
        includeImages: Boolean = true
    ): String {
        return if (format == "JSON") {
            buildString {
                append("[\n")
                memories.forEachIndexed { index, item ->
                    append("  {\n")
                    append("    \"id\": ${item.memory.id},\n")
                    append("    \"title\": \"${item.memory.title.replace("\"", "\\\"")}\",\n")
                    append("    \"notes\": \"${item.memory.notes.replace("\"", "\\\"")}\",\n")
                    if (includeLocations) {
                        append("    \"location\": \"${item.memory.locationName.replace("\"", "\\\"")}\",\n")
                    }
                    append("    \"timestamp\": ${item.memory.timestamp},\n")
                    if (includeImages) {
                        val images = item.media.filter { it.type == "image" && it.url.isNotBlank() }
                        append("    \"images\": [\n")
                        images.forEachIndexed { imgIdx, img ->
                            append("      \"${img.url.replace("\"", "\\\"")}\"${if (imgIdx < images.size - 1) "," else ""}\n")
                        }
                        append("    ],\n")
                    }
                    append("    \"mediaCount\": ${item.media.size}\n")
                    append("  }${if (index < memories.size - 1) "," else ""}\n")
                }
                append("]")
            }
        } else {
            buildString {
                append("CHRONOVA MEMORY EXPORT\n")
                append("=======================\n")
                append("Total Memories: ${memories.size}\n")
                if (includeImages) {
                    val imageCount = memories.flatMap { it.media }.count { it.type == "image" && it.url.isNotBlank() }
                    append("Total Attached Images: $imageCount\n")
                }
                append("\n")
                memories.forEach { item ->
                    append("• ${item.memory.title}\n")
                    if (includeLocations && item.memory.locationName.isNotBlank()) {
                        append("  Location: ${item.memory.locationName}\n")
                    }
                    if (item.memory.notes.isNotBlank()) {
                        append("  Notes: ${item.memory.notes}\n")
                    }
                    if (includeImages) {
                        val images = item.media.filter { it.type == "image" && it.url.isNotBlank() }
                        if (images.isNotEmpty()) {
                            append("  Attached Images (${images.size}):\n")
                            images.forEach { img ->
                                append("    - ${img.url}\n")
                            }
                        }
                    }
                    append("\n")
                }
            }
        }
    }

    fun signOut(onComplete: () -> Unit) {
        viewModelScope.launch {
            authRepository.signOut()
            onComplete()
        }
    }

    fun toggleAutoTimelineSync(context: android.content.Context, enabled: Boolean) {
        val workManager = androidx.work.WorkManager.getInstance(context)
        if (enabled) {
            val constraints = androidx.work.Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build()
            val request = androidx.work.PeriodicWorkRequestBuilder<com.example.workers.TimelineSyncWorker>(
                1, java.util.concurrent.TimeUnit.DAYS
            ).setConstraints(constraints).build()
            workManager.enqueueUniquePeriodicWork(
                "TimelineSync",
                androidx.work.ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        } else {
            workManager.cancelUniqueWork("TimelineSync")
        }
    }

    fun triggerSyncNow(context: android.content.Context) {
        val workManager = androidx.work.WorkManager.getInstance(context)
        val request = androidx.work.OneTimeWorkRequestBuilder<com.example.workers.TimelineSyncWorker>().build()
        workManager.enqueue(request)
    }
}
