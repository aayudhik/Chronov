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
    val userEmailOrPhone: String? = null
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
            userEmailOrPhone = user?.email ?: user?.phoneNumber
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProfileUiState()
    )

    fun signOut(onComplete: () -> Unit) {
        viewModelScope.launch {
            authRepository.signOut()
            onComplete()
        }
    }
}
