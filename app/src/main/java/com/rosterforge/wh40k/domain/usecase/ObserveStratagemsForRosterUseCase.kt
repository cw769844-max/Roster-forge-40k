package com.rosterforge.wh40k.domain.usecase

import com.rosterforge.wh40k.domain.model.Stratagem
import com.rosterforge.wh40k.domain.repository.RosterRepository
import com.rosterforge.wh40k.domain.repository.StratagemRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class ObserveStratagemsForRosterUseCase @Inject constructor(
    private val rosterRepository: RosterRepository,
    private val stratagemRepository: StratagemRepository,
) {
    operator fun invoke(rosterId: String): Flow<List<Stratagem>> =
        rosterRepository.observeRoster(rosterId).flatMapLatest { roster ->
            if (roster == null) flowOf(emptyList())
            else stratagemRepository.observeStratagemsForArmy(
                factionId = roster.factionId,
                detachmentId = roster.detachmentId,
            )
        }
}
