package com.rosterforge.wh40k.domain.model

/**
 * Read-only snapshot of catalogue data needed by validation and points calculation.
 * Constructed by the repository layer for a specific faction + detachment combination.
 */
data class CatalogueSnapshot(
    val faction: Faction,
    val detachment: Detachment,
    val unitsById: Map<String, Unit>,
    val enhancementsById: Map<String, Enhancement>,
)
