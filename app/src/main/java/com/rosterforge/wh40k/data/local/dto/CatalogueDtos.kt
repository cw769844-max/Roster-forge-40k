package com.rosterforge.wh40k.data.local.dto

import kotlinx.serialization.Serializable

/**
 * Serialisable DTOs used inside the `*Json` columns of [com.rosterforge.wh40k.data.local.entity]
 * tables. These mirror domain shapes but use only kotlinx.serialization-friendly types.
 */

@Serializable
data class UnitStatsDto(
    val movement: String,
    val toughness: Int,
    val save: String,
    val invulnerableSave: String? = null,
    val wounds: Int,
    val leadership: String,
    val objectiveControl: Int,
)

@Serializable
data class ModelGroupDto(
    val name: String,
    val minCount: Int,
    val maxCount: Int,
)

@Serializable
data class WeaponProfileDto(
    val id: String,
    val name: String,
    val type: String,                  // "RANGED" | "MELEE"
    val range: String,
    val attacks: String,
    val skill: String,
    val strength: String,
    val ap: String,
    val damage: String,
    val keywords: List<String>,
    val abilities: List<String>,
)

@Serializable
data class AbilityDto(
    val id: String,
    val name: String,
    val effect: String,
    val type: String,                  // CORE / FACTION / DETACHMENT / UNIT / DAMAGED / AURA
    val phase: String? = null,
)

@Serializable
data class WargearOptionDto(
    val id: String,
    val description: String,
    val constraints: WargearConstraintsDto,
    val choices: List<WargearChoiceDto>,
)

@Serializable
data class WargearChoiceDto(
    val id: String,
    val name: String,
    val pointsCost: Int = 0,
    val mutuallyExclusiveWith: List<String> = emptyList(),
)

@Serializable
data class WargearConstraintsDto(
    val minSelections: Int = 0,
    val maxSelections: Int = 1,
    val modelScope: String = "PER_UNIT",
    val perModelCount: Int = 0,
)

@Serializable
data class PointsCostDto(
    val modelCount: Int,
    val points: Int,
)

@Serializable
data class LeaderAbilityDto(
    val effect: String,
    val attachKeywords: List<String>,
)

@Serializable
data class SelectedWargearDto(
    val optionId: String,
    val choiceId: String,
    val count: Int = 1,
)
