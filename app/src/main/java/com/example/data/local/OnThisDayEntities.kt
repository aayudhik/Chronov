package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "on_this_day_memories")
data class OnThisDayMemory(
    @PrimaryKey val memoryId: Long,
    val isDismissed: Boolean = false,
    val isFavorite: Boolean = false,
    val aiComparison: String = ""
)

@Entity(tableName = "on_this_day_settings")
data class OnThisDaySettings(
    @PrimaryKey val id: Int = 1,
    val lastViewedDate: String = "",
    val isEnabled: Boolean = true,
    val isNotificationEnabled: Boolean = true,
    val notificationTimeHour: Int = 9,
    val notificationTimeMinute: Int = 0,
    val includeAiComparison: Boolean = true,
    val includeNearbyDates: Boolean = true
)

@Entity(tableName = "notification_history")
data class NotificationHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val type: String,
    val title: String,
    val body: String
)
