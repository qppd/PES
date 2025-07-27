package com.qppd.pesapp.models

data class Settings(
    val notificationsEnabled: Boolean = true,
    val emailNotificationsEnabled: Boolean = true,
    val darkModeEnabled: Boolean = false,
    val language: String = "English",
    val backupEnabled: Boolean = true
)
