sed -i '/interface MemoryDao {/a\
    @Transaction\
    @Query("SELECT * FROM memories WHERE userId = :userId AND isDraft = 0 ORDER BY timestamp DESC")\
    fun getAllMemoriesWithMediaForUser(userId: String): Flow<List<MemoryWithMedia>>\
\
    @Transaction\
    @Query("SELECT * FROM memories WHERE isDraft = 0 ORDER BY timestamp DESC")\
    fun getAllMemoriesWithMedia(): Flow<List<MemoryWithMedia>>\
\
    @Transaction\
    @Query("SELECT * FROM memories WHERE isDraft = 1 ORDER BY timestamp DESC")\
    fun getDraftMemoriesWithMedia(): Flow<List<MemoryWithMedia>>\
' app/src/main/java/com/example/data/local/MemoryDao.kt
