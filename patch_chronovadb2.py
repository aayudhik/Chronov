import re

with open("app/src/main/java/com/example/data/local/ChronovaDatabase.kt", "r") as f:
    content = f.read()

# Add entities
content = content.replace(
    "@Database(entities = [Memory::class, Media::class, SearchMessage::class, SmartCollection::class, LifeChapter::class, Story::class], version = 9, exportSchema = false)",
    "@Database(entities = [Memory::class, Media::class, SearchMessage::class, SmartCollection::class, LifeChapter::class, Story::class, OnThisDayMemory::class, OnThisDaySettings::class, NotificationHistory::class], version = 10, exportSchema = false)"
)

# Add dao
content = content.replace(
    "abstract fun memoryDao(): MemoryDao",
    "abstract fun memoryDao(): MemoryDao\n    abstract fun onThisDayDao(): OnThisDayDao"
)

# Add migration 9 to 10
migration = """
        private val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `on_this_day_memories` (`memoryId` INTEGER NOT NULL, `isDismissed` INTEGER NOT NULL, `isFavorite` INTEGER NOT NULL, `aiComparison` TEXT NOT NULL, PRIMARY KEY(`memoryId`))")
                db.execSQL("CREATE TABLE IF NOT EXISTS `on_this_day_settings` (`id` INTEGER NOT NULL, `lastViewedDate` TEXT NOT NULL, `isEnabled` INTEGER NOT NULL, `isNotificationEnabled` INTEGER NOT NULL, `notificationTimeHour` INTEGER NOT NULL, `notificationTimeMinute` INTEGER NOT NULL, `includeAiComparison` INTEGER NOT NULL, `includeNearbyDates` INTEGER NOT NULL, PRIMARY KEY(`id`))")
                db.execSQL("CREATE TABLE IF NOT EXISTS `notification_history` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `timestamp` INTEGER NOT NULL, `type` TEXT NOT NULL, `title` TEXT NOT NULL, `body` TEXT NOT NULL)")
            }
        }
"""

content = content.replace("fun getDatabase(context: Context, passphrase: kotlin.ByteArray? = null): ChronovaDatabase {", migration + "\n        fun getDatabase(context: Context, passphrase: kotlin.ByteArray? = null): ChronovaDatabase {")

# Add migration to list
content = content.replace("MIGRATION_7_8, MIGRATION_8_9)", "MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10)")

with open("app/src/main/java/com/example/data/local/ChronovaDatabase.kt", "w") as f:
    f.write(content)

