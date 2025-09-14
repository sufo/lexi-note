package com.sufo.lexinote.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// Define the data class to hold all user preferences
data class UserPreferences(
    val themeMode: String, // "LIGHT", "DARK", or "SYSTEM"
    val notificationsEnabled: Boolean
)

// At the top level of your kotlin file:
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsDataStore @Inject constructor(@ApplicationContext private val context: Context) {

    private object PreferencesKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
    }

    /**
     * A single flow that emits the combined UserPreferences object whenever any preference changes.
     */
    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data
        .map { preferences ->
            mapUserPreferences(preferences)
        }

    suspend fun saveThemeMode(themeMode: String) {
        context.dataStore.edit {
            it[PreferencesKeys.THEME_MODE] = themeMode
        }
    }

    suspend fun saveNotificationsEnabled(isEnabled: Boolean) {
        context.dataStore.edit {
            it[PreferencesKeys.NOTIFICATIONS_ENABLED] = isEnabled
        }
    }

    private fun mapUserPreferences(preferences: Preferences): UserPreferences {
        val themeMode = preferences[PreferencesKeys.THEME_MODE] ?: "SYSTEM"
        val notificationsEnabled = preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] ?: true
        return UserPreferences(themeMode, notificationsEnabled)
    }
}