package com.rosterforge.wh40k.domain.usecase

import com.rosterforge.wh40k.domain.model.CatalogueSnapshot
import com.rosterforge.wh40k.domain.model.RosterUnit

/**
 * Computes the points value of a single roster unit by selecting the matching
 * [com.rosterforge.wh40k.domain.model.PointsCost] tier and adding any selected
 * enhancement cost. Wargear-driven point modifiers (rare in 10e) are also summed.
 */
object PointsCalculator {
    fun compute(unit: RosterUnit, snapshot: CatalogueSnapshot): Int {
        val catalogueUnit = snapshot.unitsById[unit.unitId] ?: return unit.computedPoints
        val tier = catalogueUnit.pointsCosts
            .sortedBy { it.modelCount }
            .lastOrNull { it.modelCount <= unit.modelCount }
            ?: catalogueUnit.pointsCosts.firstOrNull()
            ?: return 0
        val enhancementPoints = unit.selectedEnhancementId
            ?.let { snapshot.enhancementsById[it]?.points }
            ?: 0
        val wargearPoints = unit.selectedWargear.sumOf { selected ->
            catalogueUnit.wargearOptions
                .firstOrNull { it.id == selected.optionId }
                ?.choices?.firstOrNull { it.id == selected.choiceId }
                ?.pointsCost
                ?.times(selected.count)
                ?: 0
        }
        return tier.points + enhancementPoints + wargearPoints
    }
}
