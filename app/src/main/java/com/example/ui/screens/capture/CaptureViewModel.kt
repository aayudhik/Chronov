package com.example.ui.screens.capture

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.Media
import com.example.data.local.Memory
import com.example.data.repository.MemoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CaptureUiState(
    val title: String = "",
    val description: String = "",
    val location: String = "",
    val mood: String = "",
    val weather: String = "",
    val tags: String = "",
    val photos: String = "",
    val videos: String = "",
    val voiceNotes: String = "",
    val isFavorite: Boolean = false,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false
)

class CaptureViewModel(private val repository: MemoryRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(CaptureUiState())
    val uiState: StateFlow<CaptureUiState> = _uiState.asStateFlow()

    fun updateTitle(title: String) {
        _uiState.update { it.copy(title = title) }
    }

    fun updateDescription(desc: String) {
        _uiState.update { it.copy(description = desc) }
    }

    fun updateLocation(location: String) {
        _uiState.update { it.copy(location = location) }
    }

    fun updateMood(mood: String) {
        _uiState.update { it.copy(mood = mood) }
    }

    fun updateWeather(weather: String) {
        _uiState.update { it.copy(weather = weather) }
    }

    fun updateTags(tags: String) {
        _uiState.update { it.copy(tags = tags) }
    }

    fun updatePhotos(photos: String) {
        _uiState.update { it.copy(photos = photos) }
    }

    fun updateVideos(videos: String) {
        _uiState.update { it.copy(videos = videos) }
    }

    fun updateVoiceNotes(voiceNotes: String) {
        _uiState.update { it.copy(voiceNotes = voiceNotes) }
    }

    fun updateFavorite(isFavorite: Boolean) {
        _uiState.update { it.copy(isFavorite = isFavorite) }
    }

    fun saveMemory() {
        val currentState = _uiState.value
        _uiState.update { it.copy(isSaving = true) }
        
        viewModelScope.launch {
            val memory = Memory(
                timestamp = System.currentTimeMillis(),
                title = currentState.title,
                notes = currentState.description,
                locationName = currentState.location,
                sentiment = currentState.mood,
                temperature = currentState.weather,
                isHero = currentState.isFavorite,
                score = (0..100).random()
            )
            
            val mediaList = mutableListOf<Media>()
            
            // Add photos
            if (currentState.photos.isNotBlank()) {
                currentState.photos.split(",").forEach { url ->
                    val trimmed = url.trim()
                    if (trimmed.isNotEmpty()) {
                        mediaList.add(Media(memoryId = 0, type = "image", url = trimmed))
                    }
                }
            }
            
            // Add videos
            if (currentState.videos.isNotBlank()) {
                currentState.videos.split(",").forEach { url ->
                    val trimmed = url.trim()
                    if (trimmed.isNotEmpty()) {
                        mediaList.add(Media(memoryId = 0, type = "video", url = trimmed))
                    }
                }
            }
            
            // Add voice notes
            if (currentState.voiceNotes.isNotBlank()) {
                currentState.voiceNotes.split(",").forEach { url ->
                    val trimmed = url.trim()
                    if (trimmed.isNotEmpty()) {
                        mediaList.add(Media(memoryId = 0, type = "audio", url = trimmed))
                    }
                }
            }
            
            // Add tags
            if (currentState.tags.isNotBlank()) {
                currentState.tags.split(",").forEach { tag ->
                    val trimmed = tag.trim()
                    if (trimmed.isNotEmpty()) {
                        mediaList.add(Media(memoryId = 0, type = "tag", label = trimmed))
                    }
                }
            }
            
            repository.insertMemoryWithMedia(memory, mediaList)
            
            _uiState.update { it.copy(isSaving = false, isSaved = true) }
        }
    }
}
