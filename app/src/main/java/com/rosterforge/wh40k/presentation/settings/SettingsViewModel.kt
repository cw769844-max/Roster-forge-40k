package com.rosterforge.wh40k.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rosterforge.wh40k.domain.repository.AppSettingsRepository
import com.rosterforge.wh40k.domain.repository.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val appSettings: AppSettingsRepository,
) : ViewModel() {

    val state: StateFlow<SettingsUiState> = combine(
        appSettings.installedDataVersion,
        appSettings.installedDataDate,
        appSettings.themeMode,
    ) { version, date, theme ->
        SettingsUiState(
            installedVersion = version,
            installedDate = date,
            themeMode = theme,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState(),
    )

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { appSettings.setThemeMode(mode) }
    }
}

data class SettingsUiState(
    val installedVersion: String? = null,
    val installedDate: Long? = null,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
)
