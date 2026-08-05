sed -i '/val allMemories/i\
    val allStories: Flow<List<com.example.data.local.Story>> = database.memoryDao().getAllStories()\
\
    fun getStoryById(id: Long) = database.memoryDao().getStoryById(id)\
\
    suspend fun insertStory(story: com.example.data.local.Story) = database.memoryDao().insertStory(story)\
\
    suspend fun deleteStory(id: Long) = database.memoryDao().deleteStory(id)\
' app/src/main/java/com/example/data/repository/MemoryRepository.kt
