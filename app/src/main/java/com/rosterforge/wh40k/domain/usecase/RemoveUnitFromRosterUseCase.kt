package com.rosterforge.wh40k.domain.usecase

import com.rosterforge.wh40k.domain.repository.RosterRepository
import javax.inject.Inject

class RemoveUnitFromRosterUseCase @Inject constructor(
    private val rosterRepository: RosterRepository,
) {
    suspend operator fun invoke(rosterId: String, rosterUnitId: String): Result<Unit> =
        runCatching {
            val roster = rosterRepository.getRoster(rosterId) ?: return@runCatching
            // Detach any other unit that has this one attached as its leader.
            roster.units
                .filter { it.attachedLeaderRosterUnitId == rosterUnitId }
                .forEach { detachedFrom ->
                    rosterRepository.upsertRosterUnit(
                        detachedFrom.copy(attachedLeaderRosterUnitId = null),
                    )
                }
            rosterRepository.deleteRosterUnit(rosterUnitId)
        }
}
