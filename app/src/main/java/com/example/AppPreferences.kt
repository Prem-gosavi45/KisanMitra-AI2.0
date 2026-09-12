package com.example

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "kisanmitra_prefs")

class AppPreferences(private val context: Context) {
    companion object {
        val LANGUAGE_KEY = stringPreferencesKey("language")
        val ROLE_KEY = stringPreferencesKey("role")
        val IS_DARK_MODE_KEY = androidx.datastore.preferences.core.booleanPreferencesKey("is_dark_mode")
    }

    val languageFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[LANGUAGE_KEY] ?: "en"
    }

    val roleFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[ROLE_KEY]
    }

    val isDarkModeFlow: Flow<Boolean?> = context.dataStore.data.map { preferences ->
        preferences[IS_DARK_MODE_KEY]
    }

    suspend fun saveLanguage(languageCode: String) {
        context.dataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = languageCode
        }
    }

    suspend fun saveRole(role: String) {
        context.dataStore.edit { preferences ->
            preferences[ROLE_KEY] = role
        }
    }

    suspend fun saveIsDarkMode(isDarkMode: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_DARK_MODE_KEY] = isDarkMode
        }
    }

    suspend fun clearOnboarding() {
        context.dataStore.edit { preferences ->
            preferences.remove(ROLE_KEY)
        }
    }
}