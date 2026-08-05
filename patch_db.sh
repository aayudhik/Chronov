sed -i 's/val source: String = "" \/\/ e.g., "Photos", "Calendar", "Location"/val source: String = "",\n    val latitude: Double? = null,\n    val longitude: Double? = null/' app/src/main/java/com/example/data/local/Entities.kt
sed -i 's/version = 7/version = 8/' app/src/main/java/com/example/data/local/ChronovaDatabase.kt
sed -i '/fun getDatabase/i\
        private val MIGRATION_7_8 = object : Migration(7, 8) {\
            override fun migrate(db: SupportSQLiteDatabase) {\
                db.execSQL("ALTER TABLE memories ADD COLUMN latitude REAL")\
                db.execSQL("ALTER TABLE memories ADD COLUMN longitude REAL")\
            }\
        }\
' app/src/main/java/com/example/data/local/ChronovaDatabase.kt
sed -i 's/MIGRATION_5_6, MIGRATION_6_7)/MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8)/' app/src/main/java/com/example/data/local/ChronovaDatabase.kt
