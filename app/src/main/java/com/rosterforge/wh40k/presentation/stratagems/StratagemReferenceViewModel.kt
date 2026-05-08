package com.rosterforge.wh40k.presentation.stratagems

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rosterforge.wh40k.domain.model.GamePhase
import com.rosterforge.wh40k.domain.model.Stratagem
import com.rosterforge.wh40k.domain.usecase.ObserveStratagemsForRosterUseCase
import com.rosterforge.wh40k.presentation.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class StratagemReferenceViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeStratagems: ObserveStratagemsForRosterUseCase,
) : ViewModel() {

    private val rosterId: String =
        savedStateHandle[Screen.StratagemReferenceForArmy.ARG_ROSTER_ID] ?: ""

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _phaseFilter = MutableStateFlow<GamePhase?>(null)
    val phaseFilter: StateFlow<GamePhase?> = _phaseFilter.asStateFlow()

    private val source = observeStratagems(rosterId)

    val state: StateFlow<List<Stratagem>> = combine(source, _query, _phaseFilter) {
        list, q, phase ->
        list.filter { strat ->
            (phase == null || strat.phase == phase || strat.phase == GamePhase.ANY) &&
                (q.isBlank() ||
                    strat.name.contains(q, ignoreCase = true) ||
                    strat.effect.contains(q, ignoreCase = true))
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    fun onQueryChange(text: String) { _query.value = text }
    fun onPhaseChange(phase: GamePhase?) { _phaseFilter.value = phase }
}
