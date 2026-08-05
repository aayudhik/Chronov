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
