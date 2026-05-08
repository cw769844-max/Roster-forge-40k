package com.rosterforge.wh40k.presentation.unit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rosterforge.wh40k.domain.model.BattlefieldRole
import com.rosterforge.wh40k.domain.model.Unit
import com.rosterforge.wh40k.domain.repository.CatalogueRepository
import com.rosterforge.wh40k.domain.repository.RosterRepository
import com.rosterforge.wh40k.domain.usecase.AddUnitToRosterUseCase
import com.rosterforge.wh40k.presentation.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UnitBrowserViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val catalogueRepository: CatalogueRepository,
    private val rosterRepository: RosterRepository,
    private val addUnit: AddUnitToRosterUseCase,
) : ViewModel() {

    val rosterId: String = savedStateHandle[Screen.UnitBrowser.ARG_ROSTER_ID] ?: ""

    private val _state = MutableStateFlow<UnitBrowserUiState>(UnitBrowserUiState.Loading)
    val state: StateFlow<UnitBrowserUiState> = _state.asStateFlow()

    private val _events = Channel<UnitBrowserEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        viewModelScope.launch { load() }
    }

    private suspend fun load() {
        val roster = rosterRepository.getRoster(rosterId)
        if (roster == null) {
            _state.value = UnitBrowserUiState.Error("Roster not found")
            return
        }
        val units = catalogueRepository.getUnitsForFaction(roster.factionId)
        _state.value = UnitBrowserUiState.Success(
            unitsByRole = units.groupBy { it.role }.toSortedMap(compareBy { it.ordinal }),
        )
    }

    fun onUnitChosen(unit: Unit) {
        viewModelScope.launch {
            addUnit(rosterId, unit.id).onSuccess { rosterUnitId ->
                _events.trySend(UnitBrowserEvent.UnitAdded(rosterId, rosterUnitId))
            }.onFailure { cause ->
                _events.trySend(UnitBrowserEvent.Failed(cause.message ?: "Could not add unit"))
            }
        }
    }
}

sealed interface UnitBrowserUiState {
    object Loading : UnitBrowserUiState
    data class Success(
        val unitsByRole: Map<BattlefieldRole, List<Unit>>,
    ) : UnitBrowserUiState
    data class Error(val message: String) : UnitBrowserUiState
}

sealed interface UnitBrowserEvent {
    data class UnitAdded(val rosterId: String, val rosterUnitId: String) : UnitBrowserEvent
    data class Failed(val message: String) : UnitBrowserEvent
}
