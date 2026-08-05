import re

with open("app/src/main/java/com/example/data/local/ChronovaDatabase.kt", "r") as f:
    content = f.read()

replacement = """
        fun getDatabase(context: Context, passphrase: kotlin.ByteArray? = null): ChronovaDatabase {
            return INSTANCE ?: synchronized(this) {
                val builder = Room.databaseBuilder(
                    context.applicationContext,
                    ChronovaDatabase::class.java,
                    "chronova_database"
                )
                .addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9)
                .fallbackToDestructiveMigration(dropAllTables = false)
                
                if (passphrase != null) {
                    builder.openHelperFactory(SupportFactory(passphrase))
                }
                
                val instance = builder.build()
                INSTANCE = instance
                instance
            }
        }
"""

content = re.sub(r'fun getDatabase\(context: Context\): ChronovaDatabase \{.*?\n        \}', replacement.strip(), content, flags=re.DOTALL)

with open("app/src/main/java/com/example/data/local/ChronovaDatabase.kt", "w") as f:
    f.write(content)

