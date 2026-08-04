package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Memory::class, Media::class], version = 1, exportSchema = false)
abstract class ChronovaDatabase : RoomDatabase() {
    abstract fun memoryDao(): MemoryDao

    companion object {
        @Volatile
        private var INSTANCE: ChronovaDatabase? = null

        fun getDatabase(context: Context): ChronovaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ChronovaDatabase::class.java,
                    "chronova_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
