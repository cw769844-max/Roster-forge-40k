package com.rosterforge.wh40k.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "factions")
data class FactionEntity(
    @PrimaryKey val id: String,
    val name: String,
    val abbreviation: String,
    val factionKeyword: String,
    val allegiance: String,
    val subFactionsJson: String,
)

@Entity(
    tableName = "detachments",
    foreignKeys = [
        ForeignKey(
            entity = FactionEntity::class,
            parentColumns = ["id"],
            childColumns = ["factionId"],
            onDelete = CASCADE,
        ),
    ],
    indices = [Index("factionId")],
)
data class DetachmentEntity(
    @PrimaryKey val id: String,
    val factionId: String,
    val name: String,
    val ruleName: String,
    val ruleEffect: String,
)

@Entity(
    tableName = "units",
    foreignKeys = [
        ForeignKey(
            entity = FactionEntity::class,
            parentColumns = ["id"],
            childColumns = ["factionId"],
            onDelete = CASCADE,
        ),
    ],
    indices = [Index("factionId")],
)
data class UnitEntity(
    @PrimaryKey val id: String,
    val factionId: String,
    val name: String,
    val role: String,
    val keywordsJson: String,
    val factionKeywordsJson: String,
    val statsJson: String,
    val modelGroupsJson: String,
    val weaponsJson: String,
    val abilitiesJson: String,
    val wargearOptionsJson: String,
    val pointsCostsJson: String,
    val leaderAbilityJson: String?,
    val attachmentTargetsJson: String,
    val isNamedCharacter: Boolean,
    val maxPerRoster: Int,
    val minModels: Int,
    val maxModels: Int,
)

@Entity(
    tableName = "enhancements",
    foreignKeys = [
        ForeignKey(
            entity = DetachmentEntity::class,
            parentColumns = ["id"],
            childColumns = ["detachmentId"],
            onDelete = CASCADE,
        ),
    ],
    indices = [Index("detachmentId"), Index("factionId")],
)
data class EnhancementEntity(
    @PrimaryKey val id: String,
    val detachmentId: String,
    val factionId: String,
    val name: String,
    val points: Int,
    val effect: String,
    val eligibilityKeywordsJson: String,
    val restrictionsJson: String,
)

@Entity(
    tableName = "stratagems",
    indices = [Index("factionId"), Index("detachmentId")],
)
data class StratagemEntity(
    @PrimaryKey val id: String,
    val factionId: String?,
    val detachmentId: String?,
    val name: String,
    val cp: Int,
    val type: String,
    val phase: String,
    val target: String,
    val effect: String,
    val restrictions: String,
    val flavor: String?,
)

@Entity(tableName = "data_metadata")
data class DataMetadataEntity(
    @PrimaryKey val key: String,
    val value: String,
)
