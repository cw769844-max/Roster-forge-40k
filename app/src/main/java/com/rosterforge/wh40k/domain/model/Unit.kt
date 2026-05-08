package com.rosterforge.wh40k.domain.model

data class Unit(
    val id: String,
    val factionId: String,
    val name: String,
    val role: BattlefieldRole,
    val keywords: List<String>,
    val factionKeywords: List<String>,
    val stats: UnitStats,
    val modelGroups: List<ModelGroup>,
    val weapons: List<WeaponProfile>,
    val abilities: List<Ability>,
    val wargearOptions: List<WargearOption>,
    val pointsCosts: List<PointsCost>,
    val leaderAbility: LeaderAbility?,
    val attachmentTargets: List<String>,
    val isNamedCharacter: Boolean,
    val maxPerRoster: Int,            // 0 = unlimited
    val minModels: Int,
    val maxModels: Int,
)

data class UnitStats(
    val movement: String,
    val toughness: Int,
    val save: String,
    val invulnerableSave: String?,
    val wounds: Int,
    val leadership: String,
    val objectiveControl: Int,
)

data class ModelGroup(
    val name: String,
    val minCount: Int,
    val maxCount: Int,
)

data class WeaponProfile(
    val id: String,
    val name: String,
    val type: WeaponType,
    val range: String,
    val attacks: String,
    val skill: String,
    val strength: String,
    val ap: String,
    val damage: String,
    val keywords: List<String>,
    val abilities: List<String>,
)

data class Ability(
    val id: String,
    val name: String,
    val effect: String,
    val type: AbilityType,
    val phase: GamePhase? = null,
)

data class WargearOption(
    val id: String,
    val description: String,
    val constraints: WargearConstraints,
    val choices: List<WargearChoice>,
)

data class WargearChoice(
    val id: String,
    val name: String,
    val pointsCost: Int = 0,
    val mutuallyExclusiveWith: List<String> = emptyList(),
)

data class WargearConstraints(
    val minSelections: Int = 0,
    val maxSelections: Int = 1,
    val modelScope: ModelScope = ModelScope.PER_UNIT,
    val perModelCount: Int = 0,       // used when modelScope == FIXED_NUMBER
)

data class PointsCost(
    val modelCount: Int,
    val points: Int,
)

data class LeaderAbility(
    val effect: String,
    val attachKeywords: List<String>,
)
