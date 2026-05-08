package com.rosterforge.wh40k.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rosterforge.wh40k.domain.model.Roster
import com.rosterforge.wh40k.domain.repository.RosterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val rosterRepository: RosterRepository,
) : ViewModel() {

    val state: StateFlow<HomeUiState> = rosterRepository.observeRosters()
        .map { rosters -> HomeUiState.Success(rosters) as HomeUiState }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState.Loading,
        )

    fun deleteRoster(id: String) {
        viewModelScope.launch { rosterRepository.deleteRoster(id) }
    }

    fun renameRoster(id: String, newName: String) {
        if (newName.isBlank()) return
        viewModelScope.launch { rosterRepository.renameRoster(id, newName.trim()) }
    }

    fun duplicateRoster(id: String) {
        viewModelScope.launch {
            val source = rosterRepository.getRoster(id) ?: return@launch
            rosterRepository.duplicateRoster(id, "${source.name} (copy)")
        }
    }
}

sealed interface HomeUiState {
    object Loading : HomeUiState
    data class Success(val rosters: List<Roster>) : HomeUiState
}
