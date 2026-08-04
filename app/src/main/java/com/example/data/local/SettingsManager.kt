package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsManager(private val context: Context) {
    private val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    private val AUTH_SKIPPED = booleanPreferencesKey("auth_skipped")

    val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[ONBOARDING_COMPLETED] ?: false
    }

    val isAuthSkipped: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[AUTH_SKIPPED] ?: false
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun setAuthSkipped(skipped: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[AUTH_SKIPPED] = skipped
        }
    }
}
