package com.rosterforge.wh40k.domain.usecase

import com.rosterforge.wh40k.domain.repository.CatalogueRepository
import com.rosterforge.wh40k.domain.repository.RosterRepository
import javax.inject.Inject

class AssignEnhancementUseCase @Inject constructor(
    private val rosterRepository: RosterRepository,
    private val catalogueRepository: CatalogueRepository,
) {
    /** Pass `enhancementId = null` to clear the current enhancement. */
    suspend operator fun invoke(
        rosterId: String,
        rosterUnitId: String,
        enhancementId: String?,
    ): Result<Unit> = runCatching {
        val roster = rosterRepository.getRoster(rosterId) ?: error("Roster not found")
        val rosterUnit = roster.units.firstOrNull { it.id == rosterUnitId }
            ?: error("Unit not in roster")
        val snapshot = catalogueRepository
            .getCatalogueSnapshot(roster.factionId, roster.detachmentId)
            ?: error("Catalogue snapshot unavailable")
        val cUnit = snapshot.unitsById[rosterUnit.unitId]
            ?: error("Catalogue unit missing")
        val enhancement = enhancementId?.let { snapshot.enhancementsById[it] }
        if (enhancementId != null && enhancement == null) error("Unknown enhancement")
        if (enhancement != null) {
            val unitKw = (cUnit.keywords + cUnit.factionKeywords).toSet()
            val missing = enhancement.eligibilityKeywords.filter { it !in unitKw }
            require(missing.isEmpty()) {
                "${cUnit.name} is not eligible for ${enhancement.name}: needs ${missing.joinToString()}"
            }
        }
        val newPoints = PointsCalculator.compute(
            unit = rosterUnit.copy(selectedEnhancementId = enhancementId),
            snapshot = snapshot,
        )
        rosterRepository.upsertRosterUnit(
            rosterUnit.copy(
                selectedEnhancementId = enhancementId,
                computedPoints = newPoints,
            ),
        )
    }
}
