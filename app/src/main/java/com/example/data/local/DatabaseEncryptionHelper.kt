package com.example.data.local

import android.content.Context
import net.sqlcipher.database.SQLiteDatabase
import java.io.File

object DatabaseEncryptionHelper {
    fun encryptDatabase(context: Context, passphrase: ByteArray) {
        val originalFile = context.getDatabasePath("chronova_database")
        if (!originalFile.exists()) return

        val encryptedFile = File(originalFile.parent, "chronova_database_encrypted_tmp")
        
        try {
            // Check if it's already encrypted
            SQLiteDatabase.loadLibs(context)
            SQLiteDatabase.openDatabase(
                originalFile.absolutePath,
                String(passphrase),
                null,
                SQLiteDatabase.OPEN_READONLY
            ).close()
            return // Already encrypted with this passphrase
        } catch (e: Exception) {
            // It might be unencrypted
            try {
                android.database.sqlite.SQLiteDatabase.openDatabase(
                    originalFile.absolutePath,
                    null,
                    android.database.sqlite.SQLiteDatabase.OPEN_READONLY
                ).close()
                
                // It is unencrypted. Migrate it.
                SQLiteDatabase.loadLibs(context)
                val db = SQLiteDatabase.openDatabase(originalFile.absolutePath, "", null, SQLiteDatabase.OPEN_READWRITE)
                db.rawExecSQL("ATTACH DATABASE '${encryptedFile.absolutePath}' AS encrypted KEY '${String(passphrase)}';")
                db.rawExecSQL("SELECT sqlcipher_export('encrypted');")
                db.rawExecSQL("DETACH DATABASE encrypted;")
                db.close()
                
                // Replace old db with new one
                originalFile.delete()
                encryptedFile.renameTo(originalFile)
                
                // Delete wal and shm
                File(originalFile.parent, "chronova_database-wal").delete()
                File(originalFile.parent, "chronova_database-shm").delete()
            } catch (e2: Exception) {
                // If it fails, maybe it's corrupted. Just proceed.
                e2.printStackTrace()
            }
        }
    }
}
