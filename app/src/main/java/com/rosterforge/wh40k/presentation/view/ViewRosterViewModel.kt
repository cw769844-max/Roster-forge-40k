package com.rosterforge.wh40k.presentation.view

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rosterforge.wh40k.domain.model.Detachment
import com.rosterforge.wh40k.domain.model.Faction
import com.rosterforge.wh40k.domain.model.Roster
import com.rosterforge.wh40k.domain.model.RosterUnit
import com.rosterforge.wh40k.domain.model.Unit
import com.rosterforge.wh40k.domain.repository.CatalogueRepository
import com.rosterforge.wh40k.domain.repository.RosterRepository
import com.rosterforge.wh40k.presentation.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ViewRosterViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val rosterRepository: RosterRepository,
    private val catalogueRepository: CatalogueRepository,
) : ViewModel() {

    val rosterId: String = savedStateHandle[Screen.ViewRoster.ARG_ROSTER_ID] ?: ""

    private val _state = MutableStateFlow<ViewRosterUiState>(ViewRosterUiState.Loading)
    val state: StateFlow<ViewRosterUiState> = _state.asStateFlow()

    init { viewModelScope.launch { load() } }

    private suspend fun load() {
        val roster = rosterRepository.getRoster(rosterId) ?: run {
            _state.value = ViewRosterUiState.Error("Roster not found"); return
        }
        val faction = catalogueRepository.getFaction(roster.factionId) ?: run {
            _state.value = ViewRosterUiState.Error("Faction missing"); return
        }
        val detachment = catalogueRepository.getDetachment(roster.detachmentId) ?: run {
            _state.value = ViewRosterUiState.Error("Detachment missing"); return
        }
        val unitsCatalogue = roster.units
            .map { it.unitId }
            .distinct()
            .mapNotNull { id -> catalogueRepository.getUnit(id)?.let { id to it } }
            .toMap()
        _state.value = ViewRosterUiState.Success(roster, faction, detachment, unitsCatalogue)
    }
}

sealed interface ViewRosterUiState {
    object Loading : ViewRosterUiState
    data class Success(
        val roster: Roster,
        val faction: Faction,
        val detachment: Detachment,
        val unitsCatalogue: Map<String, Unit>,
    ) : ViewRosterUiState {
        val unitsByRole = roster.units.groupBy { it.role }
            .toSortedMap(compareBy { it.ordinal })
        val attachedLeaderMap: Map<String, RosterUnit> = roster.units
            .mapNotNull { bg -> bg.attachedLeaderRosterUnitId?.let { lid -> bg.id to lid } }
            .mapNotNull { (bgId, lId) ->
                roster.units.firstOrNull { it.id == lId }?.let { bgId to it }
            }
            .toMap()
    }
    data class Error(val message: String) : ViewRosterUiState
}
