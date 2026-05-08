package com.rosterforge.wh40k.presentation.create

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rosterforge.wh40k.domain.model.Detachment
import com.rosterforge.wh40k.domain.repository.CatalogueRepository
import com.rosterforge.wh40k.domain.usecase.CreateRosterUseCase
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
class DetachmentSelectViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val catalogueRepository: CatalogueRepository,
    private val createRoster: CreateRosterUseCase,
    private val drafts: RosterDraftHolder,
) : ViewModel() {

    private val factionId: String =
        savedStateHandle[Screen.DetachmentSelect.ARG_FACTION_ID] ?: ""
    private val draftId: String =
        savedStateHandle[Screen.DetachmentSelect.ARG_DRAFT_ID] ?: ""

    private val _state = MutableStateFlow<DetachmentSelectUiState>(DetachmentSelectUiState.Loading)
    val state: StateFlow<DetachmentSelectUiState> = _state.asStateFlow()

    private val _events = Channel<DetachmentSelectEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            val detachments = catalogueRepository.getDetachmentsForFaction(factionId)
            _state.value = DetachmentSelectUiState.Success(detachments)
        }
    }

    fun onDetachmentChosen(detachmentId: String) {
        viewModelScope.launch {
            val draft = drafts.get(draftId) ?: return@launch
            val name = draft.name.ifBlank { "Untitled Roster" }
            createRoster(
                name = name,
                pointsLimit = draft.pointsLimit,
                factionId = factionId,
                detachmentId = detachmentId,
            ).onSuccess { rosterId ->
                drafts.clear(draftId)
                _events.trySend(DetachmentSelectEvent.RosterCreated(rosterId))
            }.onFailure { cause ->
                _events.trySend(DetachmentSelectEvent.Failed(cause.message ?: "Unknown error"))
            }
        }
    }
}

sealed interface DetachmentSelectUiState {
    object Loading : DetachmentSelectUiState
    data class Success(val detachments: List<Detachment>) : DetachmentSelectUiState
}

sealed interface DetachmentSelectEvent {
    data class RosterCreated(val rosterId: String) : DetachmentSelectEvent
    data class Failed(val message: String) : DetachmentSelectEvent
}
