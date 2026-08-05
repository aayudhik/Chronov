sed -i 's/entities = \[Memory::class, Media::class, SearchMessage::class, SmartCollection::class, LifeChapter::class\]/entities = \[Memory::class, Media::class, SearchMessage::class, SmartCollection::class, LifeChapter::class, Story::class\]/' app/src/main/java/com/example/data/local/ChronovaDatabase.kt
sed -i 's/version = 8/version = 9/' app/src/main/java/com/example/data/local/ChronovaDatabase.kt
sed -i '/fun getDatabase/i\
        private val MIGRATION_8_9 = object : Migration(8, 9) {\
            override fun migrate(db: SupportSQLiteDatabase) {\
                db.execSQL("CREATE TABLE IF NOT EXISTS `stories` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `timestamp` INTEGER NOT NULL, `type` TEXT NOT NULL, `title` TEXT NOT NULL, `content` TEXT NOT NULL, `coverImageUrl` TEXT NOT NULL, `timeRangeStart` INTEGER NOT NULL, `timeRangeEnd` INTEGER NOT NULL)")\
            }\
        }\
' app/src/main/java/com/example/data/local/ChronovaDatabase.kt
sed -i 's/MIGRATION_6_7, MIGRATION_7_8)/MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9)/' app/src/main/java/com/example/data/local/ChronovaDatabase.kt
