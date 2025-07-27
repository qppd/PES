package com.qppd.pesapp.utils

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

data class Settings(
    val notificationsEnabled: Boolean = true,
    val emailNotificationsEnabled: Boolean = true,
    val darkModeEnabled: Boolean = false,
    val language: String = "English",
    val backupEnabled: Boolean = true
)

class SettingsManager(private val context: Context) {
    private object PreferencesKeys {
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val EMAIL_NOTIFICATIONS_ENABLED = booleanPreferencesKey("email_notifications_enabled")
        val DARK_MODE_ENABLED = booleanPreferencesKey("dark_mode_enabled")
        val LANGUAGE = stringPreferencesKey("language")
        val BACKUP_ENABLED = booleanPreferencesKey("backup_enabled")
    }

    fun getSettings(): Flow<Settings> = context.dataStore.data.map { preferences ->
        Settings(
            notificationsEnabled = preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] ?: true,
            emailNotificationsEnabled = preferences[PreferencesKeys.EMAIL_NOTIFICATIONS_ENABLED] ?: true,
            darkModeEnabled = preferences[PreferencesKeys.DARK_MODE_ENABLED] ?: false,
            language = preferences[PreferencesKeys.LANGUAGE] ?: "English",
            backupEnabled = preferences[PreferencesKeys.BACKUP_ENABLED] ?: true
        )
    }

    suspend fun saveSettings(settings: Settings) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] = settings.notificationsEnabled
            preferences[PreferencesKeys.EMAIL_NOTIFICATIONS_ENABLED] = settings.emailNotificationsEnabled
            preferences[PreferencesKeys.DARK_MODE_ENABLED] = settings.darkModeEnabled
            preferences[PreferencesKeys.LANGUAGE] = settings.language
            preferences[PreferencesKeys.BACKUP_ENABLED] = settings.backupEnabled
        }
    }
}
