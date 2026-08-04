package com.example.di

import android.content.Context
import com.example.data.local.ChronovaDatabase
import com.example.data.repository.MemoryRepository

class AppContainer(private val context: Context) {
    val database by lazy { ChronovaDatabase.getDatabase(context) }
    val authRepository by lazy { com.example.data.auth.AuthRepository() }
    val memoryRepository by lazy { MemoryRepository(database, authRepository) }
}
