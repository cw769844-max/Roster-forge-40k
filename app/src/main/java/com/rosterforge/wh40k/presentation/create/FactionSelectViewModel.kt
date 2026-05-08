package com.rosterforge.wh40k.presentation.create

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rosterforge.wh40k.domain.model.Allegiance
import com.rosterforge.wh40k.domain.model.Faction
import com.rosterforge.wh40k.domain.repository.CatalogueRepository
import com.rosterforge.wh40k.presentation.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FactionSelectViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val catalogueRepository: CatalogueRepository,
    private val drafts: RosterDraftHolder,
) : ViewModel() {

    val draftId: String = savedStateHandle[Screen.FactionSelect.ARG_DRAFT_ID] ?: ""

    private val _state = MutableStateFlow<FactionSelectUiState>(FactionSelectUiState.Loading)
    val state: StateFlow<FactionSelectUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val factions = catalogueRepository.getAllFactions()
            _state.value = FactionSelectUiState.Success(
                factionsByAllegiance = factions.groupBy { it.allegiance }
                    .toSortedMap(compareBy { it.ordinal }),
            )
        }
    }

    fun onFactionSelected(factionId: String) {
        drafts.update(draftId) { it.copy(factionId = factionId, detachmentId = null) }
    }
}

sealed interface FactionSelectUiState {
    object Loading : FactionSelectUiState
    data class Success(
        val factionsByAllegiance: Map<Allegiance, List<Faction>>,
    ) : FactionSelectUiState
}
