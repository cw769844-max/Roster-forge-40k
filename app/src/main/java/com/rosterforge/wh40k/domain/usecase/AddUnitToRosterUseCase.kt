package com.rosterforge.wh40k.domain.usecase

import com.rosterforge.wh40k.domain.model.RosterUnit
import com.rosterforge.wh40k.domain.repository.CatalogueRepository
import com.rosterforge.wh40k.domain.repository.RosterRepository
import java.util.UUID
import javax.inject.Inject

class AddUnitToRosterUseCase @Inject constructor(
    private val rosterRepository: RosterRepository,
    private val catalogueRepository: CatalogueRepository,
) {
    suspend operator fun invoke(rosterId: String, unitId: String): Result<String> = runCatching {
        val roster = rosterRepository.getRoster(rosterId)
            ?: error("Roster $rosterId not found")
        val unit = catalogueRepository.getUnit(unitId)
            ?: error("Unit $unitId not found")
        val sortOrder = (roster.units.maxOfOrNull { it.sortOrder } ?: -1) + 1
        val basePoints = unit.pointsCosts.minByOrNull { it.modelCount }?.points ?: 0
        val rosterUnit = RosterUnit(
            id = UUID.randomUUID().toString(),
            rosterId = rosterId,
            unitId = unit.id,
            unitName = unit.name,
            role = unit.role,
            modelCount = unit.minModels.coerceAtLeast(1),
            selectedWargear = emptyList(),
            attachedLeaderRosterUnitId = null,
            selectedEnhancementId = null,
            computedPoints = basePoints,
            sortOrder = sortOrder,
        )
        rosterRepository.upsertRosterUnit(rosterUnit)
        rosterUnit.id
    }
}
