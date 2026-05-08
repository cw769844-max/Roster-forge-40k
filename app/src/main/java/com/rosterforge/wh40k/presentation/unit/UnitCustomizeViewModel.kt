package com.rosterforge.wh40k.presentation.unit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rosterforge.wh40k.domain.model.RosterUnit
import com.rosterforge.wh40k.domain.model.SelectedWargear
import com.rosterforge.wh40k.domain.model.Unit
import com.rosterforge.wh40k.domain.repository.CatalogueRepository
import com.rosterforge.wh40k.domain.repository.RosterRepository
import com.rosterforge.wh40k.domain.usecase.UpdateUnitConfigurationUseCase
import com.rosterforge.wh40k.presentation.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UnitCustomizeViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val rosterRepository: RosterRepository,
    private val catalogueRepository: CatalogueRepository,
    private val updateConfig: UpdateUnitConfigurationUseCase,
) : ViewModel() {

    private val rosterId: String = savedStateHandle[Screen.UnitCustomize.ARG_ROSTER_ID] ?: ""
    private val rosterUnitId: String = savedStateHandle[Screen.UnitCustomize.ARG_ROSTER_UNIT_ID] ?: ""

    private val _state = MutableStateFlow<UnitCustomizeUiState>(UnitCustomizeUiState.Loading)
    val state: StateFlow<UnitCustomizeUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch { load() }
    }

    private suspend fun load() {
        val roster = rosterRepository.getRoster(rosterId)
            ?: run { _state.value = UnitCustomizeUiState.Error("Roster not found"); return }
        val rUnit = roster.units.firstOrNull { it.id == rosterUnitId }
            ?: run { _state.value = UnitCustomizeUiState.Error("Unit not in roster"); return }
        val catalogue = catalogueRepository.getUnit(rUnit.unitId)
            ?: run { _state.value = UnitCustomizeUiState.Error("Catalogue unit missing"); return }
        _state.value = UnitCustomizeUiState.Success(rUnit, catalogue)
    }

    fun onModelCountChanged(newCount: Int) {
        viewModelScope.launch {
            updateConfig(rosterId, rosterUnitId, newModelCount = newCount).onSuccess { load() }
        }
    }

    fun onWargearToggled(optionId: String, choiceId: String, selected: Boolean) {
        val current = (state.value as? UnitCustomizeUiState.Success)?.rosterUnit ?: return
        val updated = if (selected) {
            current.selectedWargear + SelectedWargear(optionId, choiceId, count = 1)
        } else {
            current.selectedWargear.filterNot { it.optionId == optionId && it.choiceId == choiceId }
        }
        viewModelScope.launch {
            updateConfig(rosterId, rosterUnitId, newWargear = updated).onSuccess { load() }
        }
    }
}

sealed interface UnitCustomizeUiState {
    object Loading : UnitCustomizeUiState
    data class Success(val rosterUnit: RosterUnit, val unit: Unit) : UnitCustomizeUiState
    data class Error(val message: String) : UnitCustomizeUiState
}
