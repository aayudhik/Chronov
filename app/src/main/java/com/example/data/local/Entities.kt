package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "memories")
data class Memory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String = "",
    val timestamp: Long,
    val title: String = "",
    val locationName: String = "",
    val sentiment: String = "",
    val score: Int = 0,
    val notes: String = "",
    val aiSummary: String = "",
    val isHero: Boolean = false,
    val temperature: String = "",
    // AI Generated Fields
    val aiTitleSuggestion: String = "",
    val aiEmotionDetection: String = "",
    val aiActivityDetection: String = "",
    val aiPlaceDetection: String = "",
    val aiWeatherSummary: String = "",
    val aiImportanceScore: Int = 0,
    val aiCategory: String = "",
    val aiStory: String = "",
    
    // Automatic Timeline Generation
    val isDraft: Boolean = false,
    val confidenceScore: Int = 100,
    val source: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null
)

@Entity(tableName = "media")
data class Media(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val memoryId: Long,
    val type: String, // "image", "audio", "tag"
    val url: String = "",
    val label: String = "" 
)

@Entity(tableName = "search_messages")
data class SearchMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val isUser: Boolean,
    val text: String,
    val matchedMemoryIds: String = "" // comma separated IDs
)

@Entity(tableName = "smart_collections")
data class SmartCollection(
    @PrimaryKey val title: String,
    val isPinned: Boolean = false,
    val customCoverUrl: String = ""
)

@Entity(tableName = "stories")
data class Story(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val type: String,
    val title: String,
    val content: String,
    val coverImageUrl: String = "",
    val timeRangeStart: Long = 0L,
    val timeRangeEnd: Long = 0L
)

@Entity(tableName = "life_chapters")
data class LifeChapter(
    @PrimaryKey val id: String,
    val title: String,
    val startTimestamp: Long,
    val endTimestamp: Long,
    val customCoverUrl: String = "",
    val aiSummary: String = "",
    val milestones: String = "", // JSON string or comma separated
    val statistics: String = "" // JSON string
)
