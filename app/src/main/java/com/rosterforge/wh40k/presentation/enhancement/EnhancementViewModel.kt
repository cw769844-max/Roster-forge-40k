package com.rosterforge.wh40k.presentation.enhancement

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rosterforge.wh40k.domain.model.Enhancement
import com.rosterforge.wh40k.domain.repository.CatalogueRepository
import com.rosterforge.wh40k.domain.repository.RosterRepository
import com.rosterforge.wh40k.domain.usecase.AssignEnhancementUseCase
import com.rosterforge.wh40k.presentation.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EnhancementViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val rosterRepository: RosterRepository,
    private val catalogueRepository: CatalogueRepository,
    private val assign: AssignEnhancementUseCase,
) : ViewModel() {

    private val rosterId: String = savedStateHandle[Screen.Enhancement.ARG_ROSTER_ID] ?: ""
    private val rosterUnitId: String = savedStateHandle[Screen.Enhancement.ARG_ROSTER_UNIT_ID] ?: ""

    private val _state = MutableStateFlow<EnhancementUiState>(EnhancementUiState.Loading)
    val state: StateFlow<EnhancementUiState> = _state.asStateFlow()

    init { viewModelScope.launch { load() } }

    private suspend fun load() {
        val roster = rosterRepository.getRoster(rosterId) ?: return
        val rUnit = roster.units.firstOrNull { it.id == rosterUnitId } ?: return
        val cUnit = catalogueRepository.getUnit(rUnit.unitId) ?: return
        val detachment = catalogueRepository.getDetachment(roster.detachmentId) ?: return
        val unitKeywords = (cUnit.keywords + cUnit.factionKeywords).toSet()
        val takenIds = roster.units
            .filter { it.id != rosterUnitId }
            .mapNotNull { it.selectedEnhancementId }
            .toSet()
        val items = detachment.enhancements.map { e ->
            val eligible = e.eligibilityKeywords.all { it in unitKeywords }
            val taken = e.id in takenIds
            EnhancementItem(enhancement = e, eligible = eligible, takenElsewhere = taken)
        }
        _state.value = EnhancementUiState.Success(
            unitName = rUnit.unitName,
            currentSelection = rUnit.selectedEnhancementId,
            items = items,
        )
    }

    fun onSelect(enhancement: Enhancement?) {
        viewModelScope.launch {
            assign(rosterId, rosterUnitId, enhancement?.id).onSuccess { load() }
        }
    }
}

data class EnhancementItem(
    val enhancement: Enhancement,
    val eligible: Boolean,
    val takenElsewhere: Boolean,
)

sealed interface EnhancementUiState {
    object Loading : EnhancementUiState
    data class Success(
        val unitName: String,
        val currentSelection: String?,
        val items: List<EnhancementItem>,
    ) : EnhancementUiState
}
