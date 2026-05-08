package com.rosterforge.wh40k.domain.usecase

import com.rosterforge.wh40k.domain.model.Allegiance
import com.rosterforge.wh40k.domain.model.BattlefieldRole
import com.rosterforge.wh40k.domain.model.CatalogueSnapshot
import com.rosterforge.wh40k.domain.model.Detachment
import com.rosterforge.wh40k.domain.model.DetachmentRule
import com.rosterforge.wh40k.domain.model.Enhancement
import com.rosterforge.wh40k.domain.model.Faction
import com.rosterforge.wh40k.domain.model.LeaderAbility
import com.rosterforge.wh40k.domain.model.PointsCost
import com.rosterforge.wh40k.domain.model.Roster
import com.rosterforge.wh40k.domain.model.RosterUnit
import com.rosterforge.wh40k.domain.model.Unit
import com.rosterforge.wh40k.domain.model.UnitStats

/**
 * Shared fixtures for [ValidateRosterUseCase] tests. Keeps test scenarios
 * compact and readable.
 */
object RosterTestFixtures {

    val faction = Faction(
        id = "sm",
        name = "Space Marines",
        abbreviation = "SM",
        factionKeyword = "ADEPTUS ASTARTES",
        subFactions = emptyList(),
        allegiance = Allegiance.IMPERIUM,
    )

    val detachment = Detachment(
        id = "gladius",
        factionId = faction.id,
        name = "Gladius Task Force",
        rule = DetachmentRule("Combat Doctrines", "..."),
        enhancements = emptyList(),
        stratagems = emptyList(),
    )

    fun unit(
        id: String,
        name: String,
        role: BattlefieldRole = BattlefieldRole.INFANTRY,
        keywords: List<String> = listOf("INFANTRY"),
        factionKeywords: List<String> = listOf("ADEPTUS ASTARTES"),
        points: Int = 100,
        modelCount: Int = 5,
        minModels: Int = 5,
        maxModels: Int = 10,
        leaderAbility: LeaderAbility? = null,
        isNamed: Boolean = false,
        maxPerRoster: Int = 0,
    ) = Unit(
        id = id,
        factionId = faction.id,
        name = name,
        role = role,
        keywords = keywords,
        factionKeywords = factionKeywords,
        stats = UnitStats("6\"", 4, "3+", null, 2, "7", 2),
        modelGroups = emptyList(),
        weapons = emptyList(),
        abilities = emptyList(),
        wargearOptions = emptyList(),
        pointsCosts = listOf(PointsCost(modelCount, points)),
        leaderAbility = leaderAbility,
        attachmentTargets = leaderAbility?.attachKeywords.orEmpty(),
        isNamedCharacter = isNamed,
        maxPerRoster = maxPerRoster,
        minModels = minModels,
        maxModels = maxModels,
    )

    fun rosterUnit(
        unit: Unit,
        rosterId: String = "roster1",
        idSuffix: String = "1",
        modelCount: Int = unit.minModels,
        leaderId: String? = null,
        enhancementId: String? = null,
    ) = RosterUnit(
        id = "${unit.id}-$idSuffix",
        rosterId = rosterId,
        unitId = unit.id,
        unitName = unit.name,
        role = unit.role,
        modelCount = modelCount,
        selectedWargear = emptyList(),
        attachedLeaderRosterUnitId = leaderId,
        selectedEnhancementId = enhancementId,
        computedPoints = unit.pointsCosts.first().points,
    )

    fun snapshot(
        units: List<Unit>,
        enhancements: List<Enhancement> = emptyList(),
        detachment: Detachment = this.detachment,
    ) = CatalogueSnapshot(
        faction = faction,
        detachment = detachment,
        unitsById = units.associateBy { it.id },
        enhancementsById = enhancements.associateBy { it.id },
    )

    fun roster(
        units: List<RosterUnit>,
        pointsLimit: Int = 2000,
    ) = Roster(
        id = "roster1",
        name = "Test Roster",
        factionId = faction.id,
        factionName = faction.name,
        detachmentId = detachment.id,
        detachmentName = detachment.name,
        pointsLimit = pointsLimit,
        units = units,
        createdAt = 0L,
        updatedAt = 0L,
    )
}
