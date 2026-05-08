package com.rosterforge.wh40k.presentation.create

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class CreateRosterViewModel @Inject constructor(
    private val drafts: RosterDraftHolder,
) : ViewModel() {

    private val draft = drafts.newDraft()
    val draftId: String = draft.id

    private val _state = MutableStateFlow(
        CreateRosterUiState(name = draft.name, pointsLimit = draft.pointsLimit),
    )
    val state: StateFlow<CreateRosterUiState> = _state.asStateFlow()

    fun onNameChange(name: String) {
        _state.value = _state.value.copy(name = name)
        drafts.update(draftId) { it.copy(name = name) }
    }

    fun onPointsChange(limit: Int) {
        _state.value = _state.value.copy(pointsLimit = limit)
        drafts.update(draftId) { it.copy(pointsLimit = limit) }
    }

    fun canContinue(): Boolean =
        _state.value.name.isNotBlank() && _state.value.pointsLimit > 0
}

data class CreateRosterUiState(
    val name: String,
    val pointsLimit: Int,
)
