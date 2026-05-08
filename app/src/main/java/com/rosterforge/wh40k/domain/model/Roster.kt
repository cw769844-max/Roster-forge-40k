package com.rosterforge.wh40k.domain.model

data class Roster(
    val id: String,
    val name: String,
    val factionId: String,
    val factionName: String,
    val detachmentId: String,
    val detachmentName: String,
    val pointsLimit: Int,
    val units: List<RosterUnit>,
    val createdAt: Long,
    val updatedAt: Long,
) {
    val totalPoints: Int get() = units.sumOf { it.computedPoints }
    val pointsRemaining: Int get() = pointsLimit - totalPoints
    val isOverLimit: Boolean get() = totalPoints > pointsLimit
}

data class RosterUnit(
    val id: String,                          // UUID, unique per roster slot
    val rosterId: String,
    val unitId: String,                      // references Unit.id
    val unitName: String,                    // denormalised for display
    val role: BattlefieldRole,
    val modelCount: Int,
    val selectedWargear: List<SelectedWargear>,
    val attachedLeaderRosterUnitId: String?, // ID of the leader RosterUnit
    val selectedEnhancementId: String?,
    val computedPoints: Int,
    val customNotes: String? = null,
    val sortOrder: Int = 0,
)

data class SelectedWargear(
    val optionId: String,
    val choiceId: String,
    val count: Int = 1,
)
