package com.rosterforge.wh40k.data.mapper

import com.rosterforge.wh40k.data.local.AppJson
import com.rosterforge.wh40k.data.local.dto.SelectedWargearDto
import com.rosterforge.wh40k.data.local.entity.RosterEntity
import com.rosterforge.wh40k.data.local.entity.RosterUnitEntity
import com.rosterforge.wh40k.domain.model.BattlefieldRole
import com.rosterforge.wh40k.domain.model.Roster
import com.rosterforge.wh40k.domain.model.RosterUnit
import com.rosterforge.wh40k.domain.model.SelectedWargear
import kotlinx.serialization.builtins.ListSerializer

private val SelectedWargearListSerializer = ListSerializer(SelectedWargearDto.serializer())

fun RosterEntity.toDomain(units: List<RosterUnit>): Roster = Roster(
    id = id,
    name = name,
    factionId = factionId,
    factionName = factionName,
    detachmentId = detachmentId,
    detachmentName = detachmentName,
    pointsLimit = pointsLimit,
    units = units,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun Roster.toEntity(): RosterEntity = RosterEntity(
    id = id,
    name = name,
    factionId = factionId,
    factionName = factionName,
    detachmentId = detachmentId,
    detachmentName = detachmentName,
    pointsLimit = pointsLimit,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun RosterUnitEntity.toDomain(): RosterUnit {
    val wargearList = AppJson.decodeFromString(SelectedWargearListSerializer, selectedWargearJson)
        .map { SelectedWargear(it.optionId, it.choiceId, it.count) }
    return RosterUnit(
        id = id,
        rosterId = rosterId,
        unitId = unitId,
        unitName = unitName,
        role = BattlefieldRole.valueOf(role),
        modelCount = modelCount,
        selectedWargear = wargearList,
        attachedLeaderRosterUnitId = attachedLeaderRosterUnitId,
        selectedEnhancementId = selectedEnhancementId,
        computedPoints = computedPoints,
        customNotes = customNotes,
        sortOrder = sortOrder,
    )
}

fun RosterUnit.toEntity(): RosterUnitEntity = RosterUnitEntity(
    id = id,
    rosterId = rosterId,
    unitId = unitId,
    unitName = unitName,
    role = role.name,
    modelCount = modelCount,
    selectedWargearJson = AppJson.encodeToString(
        SelectedWargearListSerializer,
        selectedWargear.map { SelectedWargearDto(it.optionId, it.choiceId, it.count) },
    ),
    attachedLeaderRosterUnitId = attachedLeaderRosterUnitId,
    selectedEnhancementId = selectedEnhancementId,
    computedPoints = computedPoints,
    customNotes = customNotes,
    sortOrder = sortOrder,
)
