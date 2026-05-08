package com.rosterforge.wh40k.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rosterforge.wh40k.data.seed.SeedDataInitializer
import com.rosterforge.wh40k.domain.repository.AppSettingsRepository
import com.rosterforge.wh40k.domain.repository.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val appSettings: AppSettingsRepository,
    private val seedDataInitializer: SeedDataInitializer,
) : ViewModel() {

    private val _resetting = MutableStateFlow(false)

    val state: StateFlow<SettingsUiState> = combine(
        appSettings.installedDataVersion,
        appSettings.installedDataDate,
        appSettings.themeMode,
        _resetting,
    ) { version, date, theme, resetting ->
        SettingsUiState(
            installedVersion = version,
            installedDate = date,
            themeMode = theme,
            isResetting = resetting,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState(),
    )

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { appSettings.setThemeMode(mode) }
    }

    fun resetToSampleData() {
        if (_resetting.value) return
        viewModelScope.launch {
            _resetting.update { true }
            seedDataInitializer.resetToSeedData()
            _resetting.update { false }
        }
    }
}

data class SettingsUiState(
    val installedVersion: String? = null,
    val installedDate: Long? = null,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val isResetting: Boolean = false,
)
