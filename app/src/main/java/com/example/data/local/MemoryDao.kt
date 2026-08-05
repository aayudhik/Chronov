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
    @Query("SELECT * FROM memories WHERE userId = :userId AND isDraft = 0 ORDER BY timestamp DESC")
    fun getAllMemoriesWithMediaForUser(userId: String): Flow<List<MemoryWithMedia>>

    @Transaction
    @Query("SELECT * FROM memories WHERE isDraft = 0 ORDER BY timestamp DESC")
    fun getAllMemoriesWithMedia(): Flow<List<MemoryWithMedia>>

    @Transaction
    @Query("SELECT * FROM memories WHERE isDraft = 1 ORDER BY timestamp DESC")
    fun getDraftMemoriesWithMedia(): Flow<List<MemoryWithMedia>>

    @Transaction
    @Query("SELECT * FROM memories WHERE id = :memoryId")
    fun getMemoryWithMedia(memoryId: Long): Flow<MemoryWithMedia?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: Memory): Long

    @androidx.room.Update
    suspend fun updateMemory(memory: Memory)

    @androidx.room.Delete
    suspend fun deleteMemory(memory: Memory)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedia(media: List<Media>)

    @Query("SELECT COUNT(*) FROM memories")
    suspend fun getMemoryCount(): Int

    @Query("SELECT * FROM search_messages ORDER BY timestamp ASC")
    fun getAllSearchMessages(): Flow<List<SearchMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearchMessage(message: SearchMessage)

    @Query("DELETE FROM search_messages")
    suspend fun clearSearchHistory()

    @Query("SELECT * FROM smart_collections")
    fun getAllSmartCollections(): Flow<List<SmartCollection>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSmartCollection(collection: SmartCollection)

    @androidx.room.Delete
    suspend fun deleteSmartCollection(collection: SmartCollection)
}
