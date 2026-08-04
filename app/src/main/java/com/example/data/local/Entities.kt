package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "memories")
data class Memory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val title: String = "",
    val locationName: String = "",
    val sentiment: String = "",
    val score: Int = 0,
    val notes: String = "",
    val aiSummary: String = "",
    val isHero: Boolean = false,
    val temperature: String = ""
)

@Entity(tableName = "media")
data class Media(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val memoryId: Long,
    val type: String, // "image", "audio", "tag"
    val url: String = "",
    val label: String = "" 
)
