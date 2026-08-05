import re
with open("app/src/main/java/com/example/data/local/MemoryDao.kt", "r") as f:
    content = f.read()

# Remove all stories related lines
content = re.sub(r'\s*@Query\("SELECT \* FROM stories(.*?)\s*fun get\w*Stories\w*\(.*?\).*?\n', '', content, flags=re.DOTALL)
content = re.sub(r'\s*@Query\("SELECT \* FROM stories WHERE id = :storyId"\)\s*fun getStoryById\(storyId: Long\): Flow<Story\?>\n', '', content)
content = re.sub(r'\s*@Insert\(onConflict = OnConflictStrategy\.REPLACE\)\s*suspend fun insertStory\(story: Story\): Long\n', '', content)
content = re.sub(r'\s*@Query\("DELETE FROM stories WHERE id = :storyId"\)\s*suspend fun deleteStory\(storyId: Long\)\n', '', content)

# Remove any lingering duplicate `@Insert(onConflict = OnConflictStrategy.REPLACE)` before `@Transaction`
content = re.sub(r'@Insert\(onConflict = OnConflictStrategy\.REPLACE\)\n\s*@Transaction', '@Transaction', content)
content = re.sub(r'@Query\("SELECT \* FROM stories.*?"\)\n\s*@Transaction', '@Transaction', content)
content = re.sub(r'@Query\("SELECT \* FROM stories.*?"\)\n\s*fun getAllMemoriesWithMedia', 'fun getAllMemoriesWithMedia', content)

# Remove consecutive blank lines
content = re.sub(r'\n\s*\n', '\n\n', content)

# Find the end of the interface
end_idx = content.rfind("}")
if end_idx != -1:
    stories_methods = """
    @Query("SELECT * FROM stories ORDER BY timestamp DESC")
    fun getAllStories(): Flow<List<Story>>

    @Query("SELECT * FROM stories WHERE id = :storyId")
    fun getStoryById(storyId: Long): Flow<Story?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStory(story: Story): Long

    @Query("DELETE FROM stories WHERE id = :storyId")
    suspend fun deleteStory(storyId: Long)
"""
    content = content[:end_idx] + stories_methods + "\n}\n"

with open("app/src/main/java/com/example/data/local/MemoryDao.kt", "w") as f:
    f.write(content)

