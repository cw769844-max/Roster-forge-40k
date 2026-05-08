package com.rosterforge.wh40k.presentation.leader

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rosterforge.wh40k.domain.model.RosterUnit
import com.rosterforge.wh40k.domain.repository.CatalogueRepository
import com.rosterforge.wh40k.domain.repository.RosterRepository
import com.rosterforge.wh40k.domain.usecase.AttachLeaderUseCase
import com.rosterforge.wh40k.presentation.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LeaderAttachViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val rosterRepository: RosterRepository,
    private val catalogueRepository: CatalogueRepository,
    private val attachLeader: AttachLeaderUseCase,
) : ViewModel() {

    private val rosterId: String = savedStateHandle[Screen.LeaderAttach.ARG_ROSTER_ID] ?: ""
    private val rosterUnitId: String = savedStateHandle[Screen.LeaderAttach.ARG_ROSTER_UNIT_ID] ?: ""

    private val _state = MutableStateFlow<LeaderAttachUiState>(LeaderAttachUiState.Loading)
    val state: StateFlow<LeaderAttachUiState> = _state.asStateFlow()

    init { viewModelScope.launch { load() } }

    private suspend fun load() {
        val roster = rosterRepository.getRoster(rosterId)
            ?: run { _state.value = LeaderAttachUiState.Error("Roster not found"); return }
        val source = roster.units.firstOrNull { it.id == rosterUnitId }
            ?: run { _state.value = LeaderAttachUiState.Error("Unit not in roster"); return }
        val sourceCatalogue = catalogueRepository.getUnit(source.unitId) ?: run {
            _state.value = LeaderAttachUiState.Error("Catalogue unit missing"); return
        }
        val sourceIsLeader = sourceCatalogue.leaderAbility != null
        // The "source" can either be a leader picking a bodyguard, or a bodyguard picking a leader.
        val candidates = roster.units.filter { it.id != rosterUnitId }
        val mapped = candidates.mapNotNull { candidate ->
            val cUnit = catalogueRepository.getUnit(candidate.unitId) ?: return@mapNotNull null
            val isCandidateLeader = cUnit.leaderAbility != null
            val keywords = (cUnit.keywords + cUnit.factionKeywords).toSet()
            val (eligible, leaderId, bodyguardId) = if (sourceIsLeader && !isCandidateLeader) {
                val attachKeywords = sourceCatalogue.leaderAbility?.attachKeywords.orEmpty()
                Triple(attachKeywords.all { it in keywords }, source.id, candidate.id)
            } else if (!sourceIsLeader && isCandidateLeader) {
                val attachKeywords = cUnit.leaderAbility?.attachKeywords.orEmpty()
                val sourceKw = roster.units.firstOrNull { it.id == source.id }?.let {
                    catalogueRepository.getUnit(it.unitId)
                }?.let { (it.keywords + it.factionKeywords).toSet() } ?: emptySet()
                Triple(attachKeywords.all { it in sourceKw }, candidate.id, source.id)
            } else {
                Triple(false, "", "")
            }
            LeaderCandidate(
                rosterUnit = candidate,
                eligible = eligible,
                leaderId = leaderId.takeIf { it.isNotBlank() },
                bodyguardId = bodyguardId.takeIf { it.isNotBlank() },
            )
        }
        _state.value = LeaderAttachUiState.Success(
            sourceName = source.unitName,
            currentLeaderRosterUnitId = source.attachedLeaderRosterUnitId,
            candidates = mapped,
        )
    }

    fun onChooseCandidate(candidate: LeaderCandidate) {
        if (!candidate.eligible) return
        viewModelScope.launch {
            attachLeader(rosterId, candidate.bodyguardId!!, candidate.leaderId!!).onSuccess { load() }
        }
    }

    fun onDetach() {
        viewModelScope.launch {
            attachLeader(rosterId, rosterUnitId, leaderRosterUnitId = null).onSuccess { load() }
        }
    }
}

data class LeaderCandidate(
    val rosterUnit: RosterUnit,
    val eligible: Boolean,
    val leaderId: String?,
    val bodyguardId: String?,
)

sealed interface LeaderAttachUiState {
    object Loading : LeaderAttachUiState
    data class Success(
        val sourceName: String,
        val currentLeaderRosterUnitId: String?,
        val candidates: List<LeaderCandidate>,
    ) : LeaderAttachUiState
    data class Error(val message: String) : LeaderAttachUiState
}
