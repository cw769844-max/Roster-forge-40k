package com.rosterforge.wh40k.presentation.validation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rosterforge.wh40k.domain.model.Roster
import com.rosterforge.wh40k.domain.model.ValidationResult
import com.rosterforge.wh40k.domain.repository.CatalogueRepository
import com.rosterforge.wh40k.domain.repository.RosterRepository
import com.rosterforge.wh40k.domain.usecase.ValidateRosterUseCase
import com.rosterforge.wh40k.presentation.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ValidationViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val rosterRepository: RosterRepository,
    private val catalogueRepository: CatalogueRepository,
    private val validate: ValidateRosterUseCase,
) : ViewModel() {

    val rosterId: String = savedStateHandle[Screen.Validation.ARG_ROSTER_ID] ?: ""

    private val _state = MutableStateFlow<ValidationUiState>(ValidationUiState.Loading)
    val state: StateFlow<ValidationUiState> = _state.asStateFlow()

    init { viewModelScope.launch { load() } }

    private suspend fun load() {
        val roster = rosterRepository.getRoster(rosterId) ?: run {
            _state.value = ValidationUiState.Error("Roster not found"); return
        }
        val snapshot = catalogueRepository
            .getCatalogueSnapshot(roster.factionId, roster.detachmentId)
            ?: run { _state.value = ValidationUiState.Error("Catalogue unavailable"); return }
        val result = validate(roster, snapshot)
        _state.value = ValidationUiState.Success(roster, result)
    }
}

sealed interface ValidationUiState {
    object Loading : ValidationUiState
    data class Success(val roster: Roster, val result: ValidationResult) : ValidationUiState
    data class Error(val message: String) : ValidationUiState
}
