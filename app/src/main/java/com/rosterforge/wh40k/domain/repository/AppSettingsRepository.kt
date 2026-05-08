package com.rosterforge.wh40k.domain.repository

import kotlinx.coroutines.flow.Flow

interface AppSettingsRepository {
    val installedDataVersion: Flow<String?>
    val installedDataDate: Flow<Long?>
    val themeMode: Flow<ThemeMode>

    suspend fun setInstalledDataVersion(tag: String)
    suspend fun setInstalledDataDate(timestamp: Long)
    suspend fun setThemeMode(mode: ThemeMode)
}

enum class ThemeMode { SYSTEM, LIGHT, DARK }
