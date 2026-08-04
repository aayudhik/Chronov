package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoryDao {
    @Transaction
    @Query("SELECT * FROM memories ORDER BY timestamp DESC")
    fun getAllMemoriesWithMedia(): Flow<List<MemoryWithMedia>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: Memory): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedia(media: List<Media>)

    @Query("SELECT COUNT(*) FROM memories")
    suspend fun getMemoryCount(): Int
}
