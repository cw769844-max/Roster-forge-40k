package com.rosterforge.wh40k.domain.usecase

import com.rosterforge.wh40k.domain.model.SelectedWargear
import com.rosterforge.wh40k.domain.repository.CatalogueRepository
import com.rosterforge.wh40k.domain.repository.RosterRepository
import javax.inject.Inject

/**
 * Updates a roster unit's model count and/or wargear selections atomically and
 * recomputes points using [PointsCalculator].
 */
class UpdateUnitConfigurationUseCase @Inject constructor(
    private val rosterRepository: RosterRepository,
    private val catalogueRepository: CatalogueRepository,
) {
    suspend operator fun invoke(
        rosterId: String,
        rosterUnitId: String,
        newModelCount: Int? = null,
        newWargear: List<SelectedWargear>? = null,
    ): Result<Unit> = runCatching {
        val roster = rosterRepository.getRoster(rosterId) ?: error("Roster not found")
        val rosterUnit = roster.units.firstOrNull { it.id == rosterUnitId }
            ?: error("Unit not in roster")
        val snapshot = catalogueRepository
            .getCatalogueSnapshot(roster.factionId, roster.detachmentId)
            ?: error("Catalogue unavailable")
        val updated = rosterUnit.copy(
            modelCount = newModelCount ?: rosterUnit.modelCount,
            selectedWargear = newWargear ?: rosterUnit.selectedWargear,
        )
        val points = PointsCalculator.compute(updated, snapshot)
        rosterRepository.upsertRosterUnit(updated.copy(computedPoints = points))
    }
}
