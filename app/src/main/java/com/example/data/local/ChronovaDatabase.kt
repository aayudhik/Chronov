package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import net.sqlcipher.database.SupportFactory

@Database(entities = [Memory::class, Media::class, SearchMessage::class, SmartCollection::class, LifeChapter::class, Story::class, OnThisDayMemory::class, OnThisDaySettings::class, NotificationHistory::class], version = 10, exportSchema = false)
abstract class ChronovaDatabase : RoomDatabase() {
    abstract fun memoryDao(): MemoryDao
    abstract fun onThisDayDao(): OnThisDayDao

    companion object {
        @Volatile
        private var INSTANCE: ChronovaDatabase? = null

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE memories ADD COLUMN aiTitleSuggestion TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE memories ADD COLUMN aiEmotionDetection TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE memories ADD COLUMN aiActivityDetection TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE memories ADD COLUMN aiPlaceDetection TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE memories ADD COLUMN aiWeatherSummary TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE memories ADD COLUMN aiImportanceScore INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE memories ADD COLUMN aiCategory TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE memories ADD COLUMN aiStory TEXT NOT NULL DEFAULT ''")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `search_messages` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `timestamp` INTEGER NOT NULL, `isUser` INTEGER NOT NULL, `text` TEXT NOT NULL, `matchedMemoryIds` TEXT NOT NULL)")
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE memories ADD COLUMN isDraft INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE memories ADD COLUMN confidenceScore INTEGER NOT NULL DEFAULT 100")
                db.execSQL("ALTER TABLE memories ADD COLUMN source TEXT NOT NULL DEFAULT ''")
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `smart_collections` (`title` TEXT NOT NULL, `isPinned` INTEGER NOT NULL, `customCoverUrl` TEXT NOT NULL, PRIMARY KEY(`title`))")
            }
        }

        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `life_chapters` (`id` TEXT NOT NULL, `title` TEXT NOT NULL, `startTimestamp` INTEGER NOT NULL, `endTimestamp` INTEGER NOT NULL, `customCoverUrl` TEXT NOT NULL, `aiSummary` TEXT NOT NULL, `milestones` TEXT NOT NULL, `statistics` TEXT NOT NULL, PRIMARY KEY(`id`))")
            }
        }

        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE memories ADD COLUMN latitude REAL")
                db.execSQL("ALTER TABLE memories ADD COLUMN longitude REAL")
            }
        }

        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `stories` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `timestamp` INTEGER NOT NULL, `type` TEXT NOT NULL, `title` TEXT NOT NULL, `content` TEXT NOT NULL, `coverImageUrl` TEXT NOT NULL, `timeRangeStart` INTEGER NOT NULL, `timeRangeEnd` INTEGER NOT NULL)")
            }
        }

        
        private val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `on_this_day_memories` (`memoryId` INTEGER NOT NULL, `isDismissed` INTEGER NOT NULL, `isFavorite` INTEGER NOT NULL, `aiComparison` TEXT NOT NULL, PRIMARY KEY(`memoryId`))")
                db.execSQL("CREATE TABLE IF NOT EXISTS `on_this_day_settings` (`id` INTEGER NOT NULL, `lastViewedDate` TEXT NOT NULL, `isEnabled` INTEGER NOT NULL, `isNotificationEnabled` INTEGER NOT NULL, `notificationTimeHour` INTEGER NOT NULL, `notificationTimeMinute` INTEGER NOT NULL, `includeAiComparison` INTEGER NOT NULL, `includeNearbyDates` INTEGER NOT NULL, PRIMARY KEY(`id`))")
                db.execSQL("CREATE TABLE IF NOT EXISTS `notification_history` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `timestamp` INTEGER NOT NULL, `type` TEXT NOT NULL, `title` TEXT NOT NULL, `body` TEXT NOT NULL)")
            }
        }

        fun getDatabase(context: Context, passphrase: kotlin.ByteArray? = null): ChronovaDatabase {
            return INSTANCE ?: synchronized(this) {
                if (passphrase != null) {
                    DatabaseEncryptionHelper.encryptDatabase(context, passphrase)
                }
                
                val builder = Room.databaseBuilder(
                    context.applicationContext,
                    ChronovaDatabase::class.java,
                    "chronova_database"
                )
                .addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10)
                .fallbackToDestructiveMigration(dropAllTables = false)
                
                if (passphrase != null) {
                    builder.openHelperFactory(SupportFactory(passphrase))
                }
                
                val instance = builder.build()
                INSTANCE = instance
                instance
            }
        }
    }
}
