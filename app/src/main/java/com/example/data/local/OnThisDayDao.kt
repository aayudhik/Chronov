package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface OnThisDayDao {
    @Query("SELECT * FROM on_this_day_settings WHERE id = 1")
    fun getSettings(): Flow<OnThisDaySettings?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: OnThisDaySettings)

    @Query("SELECT * FROM on_this_day_memories WHERE memoryId = :memoryId")
    fun getOnThisDayMemory(memoryId: Long): Flow<OnThisDayMemory?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveOnThisDayMemory(memory: OnThisDayMemory)

    @Query("SELECT * FROM notification_history ORDER BY timestamp DESC")
    fun getNotificationHistory(): Flow<List<NotificationHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotificationHistory(history: NotificationHistory)

    // A fast query for fetching all memories on a specific date (MM-DD) across all years
    @Transaction
    @Query("SELECT * FROM memories WHERE isDraft = 0 AND strftime('%m-%d', timestamp / 1000, 'unixepoch') = :monthDay ORDER BY timestamp DESC")
    fun getMemoriesOnThisDay(monthDay: String): Flow<List<MemoryWithMedia>>
    
    // Nearest memories fallback (e.g., getting memories close to the given timestamp to filter in-memory)
    @Transaction
    @Query("SELECT * FROM memories WHERE isDraft = 0 ORDER BY timestamp DESC")
    fun getAllMemoriesWithMedia(): Flow<List<MemoryWithMedia>>
}
