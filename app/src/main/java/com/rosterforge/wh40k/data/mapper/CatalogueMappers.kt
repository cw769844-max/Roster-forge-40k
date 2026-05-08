package com.rosterforge.wh40k.data.mapper

import com.rosterforge.wh40k.data.local.AppJson
import com.rosterforge.wh40k.data.local.dto.AbilityDto
import com.rosterforge.wh40k.data.local.dto.LeaderAbilityDto
import com.rosterforge.wh40k.data.local.dto.ModelGroupDto
import com.rosterforge.wh40k.data.local.dto.PointsCostDto
import com.rosterforge.wh40k.data.local.dto.UnitStatsDto
import com.rosterforge.wh40k.data.local.dto.WargearChoiceDto
import com.rosterforge.wh40k.data.local.dto.WargearConstraintsDto
import com.rosterforge.wh40k.data.local.dto.WargearOptionDto
import com.rosterforge.wh40k.data.local.dto.WeaponProfileDto
import com.rosterforge.wh40k.data.local.entity.DetachmentEntity
import com.rosterforge.wh40k.data.local.entity.EnhancementEntity
import com.rosterforge.wh40k.data.local.entity.FactionEntity
import com.rosterforge.wh40k.data.local.entity.StratagemEntity
import com.rosterforge.wh40k.data.local.entity.UnitEntity
import com.rosterforge.wh40k.domain.model.Ability
import com.rosterforge.wh40k.domain.model.AbilityType
import com.rosterforge.wh40k.domain.model.Allegiance
import com.rosterforge.wh40k.domain.model.BattlefieldRole
import com.rosterforge.wh40k.domain.model.Detachment
import com.rosterforge.wh40k.domain.model.DetachmentRule
import com.rosterforge.wh40k.domain.model.Enhancement
import com.rosterforge.wh40k.domain.model.Faction
import com.rosterforge.wh40k.domain.model.GamePhase
import com.rosterforge.wh40k.domain.model.LeaderAbility
import com.rosterforge.wh40k.domain.model.ModelGroup
import com.rosterforge.wh40k.domain.model.ModelScope
import com.rosterforge.wh40k.domain.model.PointsCost
import com.rosterforge.wh40k.domain.model.Stratagem
import com.rosterforge.wh40k.domain.model.StratagemType
import com.rosterforge.wh40k.domain.model.Unit
import com.rosterforge.wh40k.domain.model.UnitStats
import com.rosterforge.wh40k.domain.model.WargearChoice
import com.rosterforge.wh40k.domain.model.WargearConstraints
import com.rosterforge.wh40k.domain.model.WargearOption
import com.rosterforge.wh40k.domain.model.WeaponProfile
import com.rosterforge.wh40k.domain.model.WeaponType
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer

private val StringList = ListSerializer(String.serializer())

fun FactionEntity.toDomain(): Faction = Faction(
    id = id,
    name = name,
    abbreviation = abbreviation,
    factionKeyword = factionKeyword,
    subFactions = AppJson.decodeFromString(StringList, subFactionsJson),
    allegiance = Allegiance.valueOf(allegiance),
)

fun Faction.toEntity(): FactionEntity = FactionEntity(
    id = id,
    name = name,
    abbreviation = abbreviation,
    factionKeyword = factionKeyword,
    allegiance = allegiance.name,
    subFactionsJson = AppJson.encodeToString(StringList, subFactions),
)

fun DetachmentEntity.toDomain(
    enhancements: List<Enhancement>,
    stratagems: List<Stratagem>,
): Detachment = Detachment(
    id = id,
    factionId = factionId,
    name = name,
    rule = DetachmentRule(name = ruleName, effect = ruleEffect),
    enhancements = enhancements,
    stratagems = stratagems,
)

fun Detachment.toEntity(): DetachmentEntity = DetachmentEntity(
    id = id,
    factionId = factionId,
    name = name,
    ruleName = rule.name,
    ruleEffect = rule.effect,
)

fun EnhancementEntity.toDomain(): Enhancement = Enhancement(
    id = id,
    detachmentId = detachmentId,
    factionId = factionId,
    name = name,
    points = points,
    effect = effect,
    eligibilityKeywords = AppJson.decodeFromString(StringList, eligibilityKeywordsJson),
    restrictions = AppJson.decodeFromString(StringList, restrictionsJson),
)

fun Enhancement.toEntity(): EnhancementEntity = EnhancementEntity(
    id = id,
    detachmentId = detachmentId,
    factionId = factionId,
    name = name,
    points = points,
    effect = effect,
    eligibilityKeywordsJson = AppJson.encodeToString(StringList, eligibilityKeywords),
    restrictionsJson = AppJson.encodeToString(StringList, restrictions),
)

fun StratagemEntity.toDomain(): Stratagem = Stratagem(
    id = id,
    factionId = factionId,
    detachmentId = detachmentId,
    name = name,
    cp = cp,
    type = StratagemType.valueOf(type),
    phase = GamePhase.valueOf(phase),
    target = target,
    effect = effect,
    restrictions = restrictions,
    flavor = flavor,
)

fun Stratagem.toEntity(): StratagemEntity = StratagemEntity(
    id = id,
    factionId = factionId,
    detachmentId = detachmentId,
    name = name,
    cp = cp,
    type = type.name,
    phase = phase.name,
    target = target,
    effect = effect,
    restrictions = restrictions,
    flavor = flavor,
)

fun UnitEntity.toDomain(): Unit {
    val stats = AppJson.decodeFromString(UnitStatsDto.serializer(), statsJson).toDomain()
    val modelGroups = AppJson.decodeFromString(
        ListSerializer(ModelGroupDto.serializer()), modelGroupsJson,
    ).map { it.toDomain() }
    val weapons = AppJson.decodeFromString(
        ListSerializer(WeaponProfileDto.serializer()), weaponsJson,
    ).map { it.toDomain() }
    val abilities = AppJson.decodeFromString(
        ListSerializer(AbilityDto.serializer()), abilitiesJson,
    ).map { it.toDomain() }
    val wargear = AppJson.decodeFromString(
        ListSerializer(WargearOptionDto.serializer()), wargearOptionsJson,
    ).map { it.toDomain() }
    val points = AppJson.decodeFromString(
        ListSerializer(PointsCostDto.serializer()), pointsCostsJson,
    ).map { PointsCost(it.modelCount, it.points) }
    val leader = leaderAbilityJson?.let {
        AppJson.decodeFromString(LeaderAbilityDto.serializer(), it).toDomain()
    }
    return Unit(
        id = id,
        factionId = factionId,
        name = name,
        role = BattlefieldRole.valueOf(role),
        keywords = AppJson.decodeFromString(StringList, keywordsJson),
        factionKeywords = AppJson.decodeFromString(StringList, factionKeywordsJson),
        stats = stats,
        modelGroups = modelGroups,
        weapons = weapons,
        abilities = abilities,
        wargearOptions = wargear,
        pointsCosts = points,
        leaderAbility = leader,
        attachmentTargets = AppJson.decodeFromString(StringList, attachmentTargetsJson),
        isNamedCharacter = isNamedCharacter,
        maxPerRoster = maxPerRoster,
        minModels = minModels,
        maxModels = maxModels,
    )
}

fun Unit.toEntity(): UnitEntity = UnitEntity(
    id = id,
    factionId = factionId,
    name = name,
    role = role.name,
    keywordsJson = AppJson.encodeToString(StringList, keywords),
    factionKeywordsJson = AppJson.encodeToString(StringList, factionKeywords),
    statsJson = AppJson.encodeToString(UnitStatsDto.serializer(), stats.toDto()),
    modelGroupsJson = AppJson.encodeToString(
        ListSerializer(ModelGroupDto.serializer()), modelGroups.map { it.toDto() },
    ),
    weaponsJson = AppJson.encodeToString(
        ListSerializer(WeaponProfileDto.serializer()), weapons.map { it.toDto() },
    ),
    abilitiesJson = AppJson.encodeToString(
        ListSerializer(AbilityDto.serializer()), abilities.map { it.toDto() },
    ),
    wargearOptionsJson = AppJson.encodeToString(
        ListSerializer(WargearOptionDto.serializer()), wargearOptions.map { it.toDto() },
    ),
    pointsCostsJson = AppJson.encodeToString(
        ListSerializer(PointsCostDto.serializer()),
        pointsCosts.map { PointsCostDto(it.modelCount, it.points) },
    ),
    leaderAbilityJson = leaderAbility?.let {
        AppJson.encodeToString(LeaderAbilityDto.serializer(), it.toDto())
    },
    attachmentTargetsJson = AppJson.encodeToString(StringList, attachmentTargets),
    isNamedCharacter = isNamedCharacter,
    maxPerRoster = maxPerRoster,
    minModels = minModels,
    maxModels = maxModels,
)

// ─────────── Inner DTO mappers ───────────

private fun UnitStatsDto.toDomain() = UnitStats(
    movement, toughness, save, invulnerableSave, wounds, leadership, objectiveControl,
)
private fun UnitStats.toDto() = UnitStatsDto(
    movement, toughness, save, invulnerableSave, wounds, leadership, objectiveControl,
)
private fun ModelGroupDto.toDomain() = ModelGroup(name, minCount, maxCount)
private fun ModelGroup.toDto() = ModelGroupDto(name, minCount, maxCount)
private fun WeaponProfileDto.toDomain() = WeaponProfile(
    id, name, WeaponType.valueOf(type), range, attacks, skill, strength, ap, damage, keywords, abilities,
)
private fun WeaponProfile.toDto() = WeaponProfileDto(
    id, name, type.name, range, attacks, skill, strength, ap, damage, keywords, abilities,
)
private fun AbilityDto.toDomain() = Ability(
    id, name, effect, AbilityType.valueOf(type), phase?.let { GamePhase.valueOf(it) },
)
private fun Ability.toDto() = AbilityDto(id, name, effect, type.name, phase?.name)
private fun WargearOptionDto.toDomain() = WargearOption(
    id = id,
    description = description,
    constraints = constraints.toDomain(),
    choices = choices.map { it.toDomain() },
)
private fun WargearOption.toDto() = WargearOptionDto(
    id = id,
    description = description,
    constraints = constraints.toDto(),
    choices = choices.map { it.toDto() },
)
private fun WargearConstraintsDto.toDomain() = WargearConstraints(
    minSelections, maxSelections, ModelScope.valueOf(modelScope), perModelCount,
)
private fun WargearConstraints.toDto() = WargearConstraintsDto(
    minSelections, maxSelections, modelScope.name, perModelCount,
)
private fun WargearChoiceDto.toDomain() = WargearChoice(id, name, pointsCost, mutuallyExclusiveWith)
private fun WargearChoice.toDto() = WargearChoiceDto(id, name, pointsCost, mutuallyExclusiveWith)
private fun LeaderAbilityDto.toDomain() = LeaderAbility(effect, attachKeywords)
private fun LeaderAbility.toDto() = LeaderAbilityDto(effect, attachKeywords)
