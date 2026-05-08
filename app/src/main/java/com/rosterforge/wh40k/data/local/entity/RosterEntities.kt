package com.rosterforge.wh40k.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "rosters")
data class RosterEntity(
    @PrimaryKey val id: String,
    val name: String,
    val factionId: String,
    val factionName: String,
    val detachmentId: String,
    val detachmentName: String,
    val pointsLimit: Int,
    val createdAt: Long,
    val updatedAt: Long,
)

@Entity(
    tableName = "roster_units",
    foreignKeys = [
        ForeignKey(
            entity = RosterEntity::class,
            parentColumns = ["id"],
            childColumns = ["rosterId"],
            onDelete = CASCADE,
        ),
    ],
    indices = [Index("rosterId")],
)
data class RosterUnitEntity(
    @PrimaryKey val id: String,
    val rosterId: String,
    val unitId: String,
    val unitName: String,
    val role: String,
    val modelCount: Int,
    val selectedWargearJson: String,
    val attachedLeaderRosterUnitId: String?,
    val selectedEnhancementId: String?,
    val computedPoints: Int,
    val customNotes: String?,
    val sortOrder: Int,
)
