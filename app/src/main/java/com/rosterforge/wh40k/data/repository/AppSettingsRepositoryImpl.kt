package com.rosterforge.wh40k.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.rosterforge.wh40k.domain.repository.AppSettingsRepository
import com.rosterforge.wh40k.domain.repository.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppSettingsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : AppSettingsRepository {

    private val keyVersion = stringPreferencesKey("installed_data_version")
    private val keyDate = longPreferencesKey("installed_data_date")
    private val keyTheme = stringPreferencesKey("theme_mode")

    override val installedDataVersion: Flow<String?> =
        dataStore.data.map { it[keyVersion] }

    override val installedDataDate: Flow<Long?> =
        dataStore.data.map { it[keyDate] }

    override val themeMode: Flow<ThemeMode> = dataStore.data.map {
        it[keyTheme]?.let(ThemeMode::valueOf) ?: ThemeMode.SYSTEM
    }

    override suspend fun setInstalledDataVersion(tag: String) {
        dataStore.edit { it[keyVersion] = tag }
    }

    override suspend fun setInstalledDataDate(timestamp: Long) {
        dataStore.edit { it[keyDate] = timestamp }
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[keyTheme] = mode.name }
    }
}
