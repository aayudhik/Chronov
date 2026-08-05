# Fix Entities.kt
sed -i '63,73d' app/src/main/java/com/example/data/local/Entities.kt
sed -i '/data class LifeChapter(/i\
@Entity(tableName = "stories")\
data class Story(\
    @PrimaryKey(autoGenerate = true) val id: Long = 0,\
    val timestamp: Long,\
    val type: String,\
    val title: String,\
    val content: String,\
    val coverImageUrl: String = "",\
    val timeRangeStart: Long = 0L,\
    val timeRangeEnd: Long = 0L\
)\
' app/src/main/java/com/example/data/local/Entities.kt

# Fix MemoryDao.kt
sed -i '14,24d' app/src/main/java/com/example/data/local/MemoryDao.kt
sed -i '/@Transaction/i\
    @Query("SELECT * FROM stories ORDER BY timestamp DESC")\
    fun getAllStories(): Flow<List<Story>>\
\
    @Query("SELECT * FROM stories WHERE id = :storyId")\
    fun getStoryById(storyId: Long): Flow<Story?>\
\
    @Insert(onConflict = OnConflictStrategy.REPLACE)\
    suspend fun insertStory(story: Story): Long\
\
    @Query("DELETE FROM stories WHERE id = :storyId")\
    suspend fun deleteStory(storyId: Long)\
' app/src/main/java/com/example/data/local/MemoryDao.kt
