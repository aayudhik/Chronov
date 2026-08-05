sed -i '/fun getAllMemoriesWithMediaForUser/i\
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
