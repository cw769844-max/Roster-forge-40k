package com.rosterforge.wh40k.data.seed

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

/**
 * Hand-crafted demo data so the app is immediately usable without depending on
 * the BattleScribe parser. Two factions are seeded with two detachments each,
 * a small set of units (including leaders), enhancements, faction stratagems,
 * and a handful of core stratagems.
 *
 * Numbers and rules text are deliberately abbreviated; this is reference
 * content for app testing, not the canonical 10e ruleset.
 */
object SampleCatalogueSeed {

    val factions: List<Faction> = listOf(
        Faction(
            id = "sm",
            name = "Space Marines",
            abbreviation = "SM",
            factionKeyword = "ADEPTUS ASTARTES",
            subFactions = listOf("Ultramarines", "Blood Angels", "Dark Angels"),
            allegiance = Allegiance.IMPERIUM,
        ),
        Faction(
            id = "necrons",
            name = "Necrons",
            abbreviation = "NEC",
            factionKeyword = "NECRONS",
            subFactions = listOf("Sautekh", "Mephrit", "Novokh"),
            allegiance = Allegiance.XENOS,
        ),
    )

    // ───────────────────────── Space Marines ─────────────────────────

    private val gladiusEnhancements = listOf(
        Enhancement(
            id = "sm-enh-bastion",
            detachmentId = "sm-gladius",
            factionId = "sm",
            name = "Bastion Plate",
            points = 15,
            effect = "Each time an attack is allocated to the bearer, " +
                "subtract 1 from the Damage characteristic of that attack.",
            eligibilityKeywords = listOf("CHARACTER"),
            restrictions = emptyList(),
        ),
        Enhancement(
            id = "sm-enh-artificer",
            detachmentId = "sm-gladius",
            factionId = "sm",
            name = "Artificer Armour",
            points = 25,
            effect = "The bearer has a 2+ save and a 4+ invulnerable save.",
            eligibilityKeywords = listOf("CHARACTER"),
            restrictions = emptyList(),
        ),
        Enhancement(
            id = "sm-enh-hood",
            detachmentId = "sm-gladius",
            factionId = "sm",
            name = "Adept of the Hood",
            points = 25,
            effect = "While the bearer leads a unit, ranged attacks targeting " +
                "that unit suffer -1 to wound rolls of natural 6.",
            eligibilityKeywords = listOf("PSYKER"),
            restrictions = emptyList(),
        ),
        Enhancement(
            id = "sm-enh-honour",
            detachmentId = "sm-gladius",
            factionId = "sm",
            name = "Honour Vehement",
            points = 35,
            effect = "The bearer's melee weapons have the [SUSTAINED HITS 1] ability.",
            eligibilityKeywords = listOf("CHARACTER"),
            restrictions = emptyList(),
        ),
    )

    private val vanguardEnhancements = listOf(
        Enhancement(
            id = "sm-enh-shadow",
            detachmentId = "sm-vanguard",
            factionId = "sm",
            name = "Shadow Master",
            points = 20,
            effect = "While the bearer is leading a unit, that unit has STEALTH.",
            eligibilityKeywords = listOf("INFANTRY", "CHARACTER"),
            restrictions = emptyList(),
        ),
        Enhancement(
            id = "sm-enh-relic-shroud",
            detachmentId = "sm-vanguard",
            factionId = "sm",
            name = "Relic Shroud",
            points = 25,
            effect = "Ranged attacks targeting the bearer's unit have -1 to wound.",
            eligibilityKeywords = listOf("CHARACTER"),
            restrictions = emptyList(),
        ),
        Enhancement(
            id = "sm-enh-fire-discipline",
            detachmentId = "sm-vanguard",
            factionId = "sm",
            name = "Fire Discipline",
            points = 30,
            effect = "While the bearer leads a unit, ranged weapons in that " +
                "unit have [LETHAL HITS].",
            eligibilityKeywords = listOf("CHARACTER"),
            restrictions = emptyList(),
        ),
    )

    private val gladiusStratagems = listOf(
        Stratagem(
            id = "sm-strat-rapid-assault",
            factionId = "sm",
            detachmentId = "sm-gladius",
            name = "Rapid Assault",
            cp = 1,
            type = StratagemType.BATTLE_TACTIC,
            phase = GamePhase.SHOOTING,
            target = "One ADEPTUS ASTARTES INFANTRY unit from your army.",
            effect = "Until the end of the phase, each time a model in your unit " +
                "makes a ranged attack, it can target a unit it Advanced toward.",
            restrictions = "",
        ),
        Stratagem(
            id = "sm-strat-honour",
            factionId = "sm",
            detachmentId = "sm-gladius",
            name = "Honour the Chapter",
            cp = 2,
            type = StratagemType.BATTLE_TACTIC,
            phase = GamePhase.FIGHT,
            target = "One ADEPTUS ASTARTES unit from your army.",
            effect = "Until the end of the phase, melee weapons equipped by " +
                "models in your unit have [LETHAL HITS] and [SUSTAINED HITS 1].",
            restrictions = "",
        ),
        Stratagem(
            id = "sm-strat-storm-fire",
            factionId = "sm",
            detachmentId = "sm-gladius",
            name = "Storm of Fire",
            cp = 1,
            type = StratagemType.BATTLE_TACTIC,
            phase = GamePhase.SHOOTING,
            target = "One ADEPTUS ASTARTES unit from your army that targeted " +
                "the unit named by Oath of Moment.",
            effect = "Until the end of the phase, ranged weapons in your unit " +
                "have [SUSTAINED HITS 1].",
            restrictions = "",
        ),
    )

    private val vanguardStratagems = listOf(
        Stratagem(
            id = "sm-strat-strike-shadows",
            factionId = "sm",
            detachmentId = "sm-vanguard",
            name = "Strike from the Shadows",
            cp = 1,
            type = StratagemType.STRATEGIC_PLOY,
            phase = GamePhase.MOVEMENT,
            target = "One ADEPTUS ASTARTES INFANTRY unit from your army in reserves.",
            effect = "Set up the unit anywhere on the battlefield more than 9\" " +
                "from any enemy units.",
            restrictions = "",
        ),
        Stratagem(
            id = "sm-strat-shock",
            factionId = "sm",
            detachmentId = "sm-vanguard",
            name = "Shock Tactics",
            cp = 1,
            type = StratagemType.BATTLE_TACTIC,
            phase = GamePhase.SHOOTING,
            target = "One ADEPTUS ASTARTES unit that arrived from reserves this turn.",
            effect = "Ranged weapons in this unit have [LETHAL HITS] for the phase.",
            restrictions = "",
        ),
    )

    val smDetachments = listOf(
        Detachment(
            id = "sm-gladius",
            factionId = "sm",
            name = "Gladius Task Force",
            rule = DetachmentRule(
                name = "Combat Doctrines",
                effect = "At the start of each battle round, you can declare " +
                    "either the Devastator, Tactical, or Assault doctrine. " +
                    "While in a doctrine, weapons in your army gain " +
                    "[LETHAL HITS] for the matching phase.",
            ),
            enhancements = gladiusEnhancements,
            stratagems = gladiusStratagems,
        ),
        Detachment(
            id = "sm-vanguard",
            factionId = "sm",
            name = "Vanguard Spearhead",
            rule = DetachmentRule(
                name = "They Shall Not See Us Coming",
                effect = "ADEPTUS ASTARTES units in your army have STEALTH " +
                    "while more than 12\" from any enemy units.",
            ),
            enhancements = vanguardEnhancements,
            stratagems = vanguardStratagems,
        ),
    )

    // SM weapons reused across multiple unit profiles.
    private val boltRifle = WeaponProfile(
        id = "wpn-bolt-rifle",
        name = "Bolt rifle",
        type = WeaponType.RANGED,
        range = "24\"",
        attacks = "2",
        skill = "3+",
        strength = "4",
        ap = "-1",
        damage = "1",
        keywords = listOf("ASSAULT", "HEAVY"),
        abilities = emptyList(),
    )
    private val heavyBolter = WeaponProfile(
        id = "wpn-heavy-bolter",
        name = "Heavy bolter",
        type = WeaponType.RANGED,
        range = "36\"",
        attacks = "3",
        skill = "3+",
        strength = "5",
        ap = "-1",
        damage = "2",
        keywords = listOf("HEAVY", "SUSTAINED HITS 1"),
        abilities = emptyList(),
    )
    private val powerFist = WeaponProfile(
        id = "wpn-power-fist",
        name = "Power fist",
        type = WeaponType.MELEE,
        range = "Melee",
        attacks = "3",
        skill = "3+",
        strength = "8",
        ap = "-2",
        damage = "2",
        keywords = emptyList(),
        abilities = emptyList(),
    )
    private val chainsword = WeaponProfile(
        id = "wpn-chainsword",
        name = "Chainsword",
        type = WeaponType.MELEE,
        range = "Melee",
        attacks = "4",
        skill = "3+",
        strength = "4",
        ap = "-1",
        damage = "1",
        keywords = listOf("LETHAL HITS"),
        abilities = emptyList(),
    )
    private val masterCraftedBoltRifle = boltRifle.copy(
        id = "wpn-mc-bolt-rifle",
        name = "Master-crafted bolt rifle",
        damage = "2",
    )

    val smUnits = listOf(
        Unit(
            id = "sm-captain",
            factionId = "sm",
            name = "Captain",
            role = BattlefieldRole.CHARACTER,
            keywords = listOf("CHARACTER", "INFANTRY", "IMPERIUM", "CAPTAIN"),
            factionKeywords = listOf("ADEPTUS ASTARTES"),
            stats = UnitStats("6\"", 4, "3+", "4+", 5, "6", 1),
            modelGroups = listOf(ModelGroup("Captain", 1, 1)),
            weapons = listOf(masterCraftedBoltRifle, powerFist),
            abilities = listOf(
                Ability(
                    id = "abl-rites",
                    name = "Rites of Battle",
                    effect = "Once per battle, in your Command phase, you can gain 1 CP.",
                    type = AbilityType.UNIT,
                ),
            ),
            wargearOptions = emptyList(),
            pointsCosts = listOf(PointsCost(1, 80)),
            leaderAbility = LeaderAbility(
                effect = "This model can be attached to ADEPTUS ASTARTES INFANTRY units.",
                attachKeywords = listOf("INFANTRY"),
            ),
            attachmentTargets = listOf("INFANTRY"),
            isNamedCharacter = false,
            maxPerRoster = 0,
            minModels = 1,
            maxModels = 1,
        ),
        Unit(
            id = "sm-lieutenant",
            factionId = "sm",
            name = "Lieutenant",
            role = BattlefieldRole.CHARACTER,
            keywords = listOf("CHARACTER", "INFANTRY", "IMPERIUM", "LIEUTENANT"),
            factionKeywords = listOf("ADEPTUS ASTARTES"),
            stats = UnitStats("6\"", 4, "3+", "4+", 4, "6", 1),
            modelGroups = listOf(ModelGroup("Lieutenant", 1, 1)),
            weapons = listOf(masterCraftedBoltRifle, chainsword),
            abilities = listOf(
                Ability(
                    id = "abl-target-priority",
                    name = "Target Priority",
                    effect = "While leading a unit, ranged weapons in that unit " +
                        "have [LETHAL HITS] when targeting the Oath of Moment unit.",
                    type = AbilityType.UNIT,
                ),
            ),
            wargearOptions = emptyList(),
            pointsCosts = listOf(PointsCost(1, 65)),
            leaderAbility = LeaderAbility(
                effect = "Attaches to ADEPTUS ASTARTES INFANTRY.",
                attachKeywords = listOf("INFANTRY"),
            ),
            attachmentTargets = listOf("INFANTRY"),
            isNamedCharacter = false,
            maxPerRoster = 0,
            minModels = 1,
            maxModels = 1,
        ),
        Unit(
            id = "sm-librarian",
            factionId = "sm",
            name = "Librarian",
            role = BattlefieldRole.CHARACTER,
            keywords = listOf("CHARACTER", "INFANTRY", "IMPERIUM", "PSYKER"),
            factionKeywords = listOf("ADEPTUS ASTARTES"),
            stats = UnitStats("6\"", 4, "3+", "4+", 4, "6", 1),
            modelGroups = listOf(ModelGroup("Librarian", 1, 1)),
            weapons = listOf(
                WeaponProfile(
                    id = "wpn-force-staff",
                    name = "Force staff",
                    type = WeaponType.MELEE,
                    range = "Melee",
                    attacks = "4",
                    skill = "2+",
                    strength = "6",
                    ap = "-1",
                    damage = "2",
                    keywords = listOf("PSYCHIC"),
                    abilities = emptyList(),
                ),
                WeaponProfile(
                    id = "wpn-smite",
                    name = "Smite",
                    type = WeaponType.RANGED,
                    range = "18\"",
                    attacks = "D3",
                    skill = "2+",
                    strength = "5",
                    ap = "-1",
                    damage = "2",
                    keywords = listOf("PSYCHIC", "DEVASTATING WOUNDS"),
                    abilities = emptyList(),
                ),
            ),
            abilities = listOf(
                Ability(
                    id = "abl-psychic-hood",
                    name = "Psychic Hood",
                    effect = "While leading a unit, enemy PSYCHIC attacks targeting " +
                        "that unit suffer -1 to hit.",
                    type = AbilityType.UNIT,
                ),
            ),
            wargearOptions = emptyList(),
            pointsCosts = listOf(PointsCost(1, 70)),
            leaderAbility = LeaderAbility(
                effect = "Attaches to ADEPTUS ASTARTES INFANTRY.",
                attachKeywords = listOf("INFANTRY"),
            ),
            attachmentTargets = listOf("INFANTRY"),
            isNamedCharacter = false,
            maxPerRoster = 0,
            minModels = 1,
            maxModels = 1,
        ),
        Unit(
            id = "sm-marneus-calgar",
            factionId = "sm",
            name = "Marneus Calgar",
            role = BattlefieldRole.EPIC_HERO,
            keywords = listOf(
                "CHARACTER", "INFANTRY", "IMPERIUM", "EPIC HERO", "CHAPTER MASTER",
            ),
            factionKeywords = listOf("ADEPTUS ASTARTES", "ULTRAMARINES"),
            stats = UnitStats("6\"", 6, "2+", "4+", 6, "6", 2),
            modelGroups = listOf(ModelGroup("Calgar", 1, 1)),
            weapons = listOf(
                WeaponProfile(
                    id = "wpn-gauntlets-ultramar",
                    name = "Gauntlets of Ultramar",
                    type = WeaponType.RANGED,
                    range = "24\"",
                    attacks = "4",
                    skill = "2+",
                    strength = "5",
                    ap = "-1",
                    damage = "2",
                    keywords = listOf("ASSAULT", "TWIN-LINKED"),
                    abilities = emptyList(),
                ),
                WeaponProfile(
                    id = "wpn-gauntlets-ultramar-melee",
                    name = "Gauntlets of Ultramar (melee)",
                    type = WeaponType.MELEE,
                    range = "Melee",
                    attacks = "6",
                    skill = "2+",
                    strength = "8",
                    ap = "-2",
                    damage = "2",
                    keywords = listOf("TWIN-LINKED"),
                    abilities = emptyList(),
                ),
            ),
            abilities = listOf(
                Ability(
                    id = "abl-master-of-the-chapter",
                    name = "Master of the Chapter",
                    effect = "Once per battle, you may gain D3 CP at the start of " +
                        "your Command phase.",
                    type = AbilityType.UNIT,
                ),
            ),
            wargearOptions = emptyList(),
            pointsCosts = listOf(PointsCost(1, 185)),
            leaderAbility = LeaderAbility(
                effect = "Attaches to ADEPTUS ASTARTES INFANTRY.",
                attachKeywords = listOf("INFANTRY"),
            ),
            attachmentTargets = listOf("INFANTRY"),
            isNamedCharacter = true,
            maxPerRoster = 1,
            minModels = 1,
            maxModels = 1,
        ),
        Unit(
            id = "sm-intercessors",
            factionId = "sm",
            name = "Intercessor Squad",
            role = BattlefieldRole.BATTLELINE,
            keywords = listOf("INFANTRY", "BATTLELINE", "IMPERIUM", "INTERCESSORS"),
            factionKeywords = listOf("ADEPTUS ASTARTES"),
            stats = UnitStats("6\"", 4, "3+", null, 2, "7", 2),
            modelGroups = listOf(
                ModelGroup("Sergeant", 1, 1),
                ModelGroup("Intercessor", 4, 9),
            ),
            weapons = listOf(boltRifle, chainsword),
            abilities = listOf(
                Ability(
                    id = "abl-objective-secured",
                    name = "Objective Secured",
                    effect = "This unit's models count as 2 for objective control.",
                    type = AbilityType.UNIT,
                ),
            ),
            wargearOptions = listOf(
                WargearOption(
                    id = "opt-int-sergeant",
                    description = "Sergeant equipment",
                    constraints = WargearConstraints(
                        minSelections = 1, maxSelections = 1,
                    ),
                    choices = listOf(
                        WargearChoice("ch-int-sgt-bolt", "Bolt rifle + chainsword"),
                        WargearChoice("ch-int-sgt-fist", "Bolt rifle + power fist"),
                    ),
                ),
            ),
            pointsCosts = listOf(
                PointsCost(5, 80),
                PointsCost(10, 160),
            ),
            leaderAbility = null,
            attachmentTargets = emptyList(),
            isNamedCharacter = false,
            maxPerRoster = 0,
            minModels = 5,
            maxModels = 10,
        ),
        Unit(
            id = "sm-assault-intercessors",
            factionId = "sm",
            name = "Assault Intercessor Squad",
            role = BattlefieldRole.BATTLELINE,
            keywords = listOf("INFANTRY", "BATTLELINE", "IMPERIUM", "ASSAULT INTERCESSORS"),
            factionKeywords = listOf("ADEPTUS ASTARTES"),
            stats = UnitStats("6\"", 4, "3+", null, 2, "7", 2),
            modelGroups = listOf(
                ModelGroup("Sergeant", 1, 1),
                ModelGroup("Assault Intercessor", 4, 9),
            ),
            weapons = listOf(chainsword),
            abilities = listOf(
                Ability(
                    id = "abl-shock-assault",
                    name = "Shock Assault",
                    effect = "Each time a model in this unit makes a melee attack " +
                        "in the same turn it ended a Charge move, add 1 to the " +
                        "Strength characteristic of that attack.",
                    type = AbilityType.UNIT,
                ),
            ),
            wargearOptions = emptyList(),
            pointsCosts = listOf(
                PointsCost(5, 75),
                PointsCost(10, 150),
            ),
            leaderAbility = null,
            attachmentTargets = emptyList(),
            isNamedCharacter = false,
            maxPerRoster = 0,
            minModels = 5,
            maxModels = 10,
        ),
        Unit(
            id = "sm-tacticals",
            factionId = "sm",
            name = "Tactical Squad",
            role = BattlefieldRole.BATTLELINE,
            keywords = listOf("INFANTRY", "BATTLELINE", "IMPERIUM"),
            factionKeywords = listOf("ADEPTUS ASTARTES"),
            stats = UnitStats("6\"", 4, "3+", null, 2, "7", 2),
            modelGroups = listOf(
                ModelGroup("Sergeant", 1, 1),
                ModelGroup("Tactical Marine", 4, 9),
            ),
            weapons = listOf(boltRifle, heavyBolter, chainsword),
            abilities = emptyList(),
            wargearOptions = emptyList(),
            pointsCosts = listOf(
                PointsCost(5, 70),
                PointsCost(10, 140),
            ),
            leaderAbility = null,
            attachmentTargets = emptyList(),
            isNamedCharacter = false,
            maxPerRoster = 0,
            minModels = 5,
            maxModels = 10,
        ),
    )

    // ───────────────────────── Necrons ─────────────────────────

    private val awakenedDynastyEnhancements = listOf(
        Enhancement(
            id = "nec-enh-veil-darkness",
            detachmentId = "nec-awakened",
            factionId = "necrons",
            name = "Veil of Darkness",
            points = 25,
            effect = "Once per battle, the bearer's unit may be redeployed " +
                "anywhere more than 9\" from enemy units.",
            eligibilityKeywords = listOf("CHARACTER"),
            restrictions = emptyList(),
        ),
        Enhancement(
            id = "nec-enh-phylacterine",
            detachmentId = "nec-awakened",
            factionId = "necrons",
            name = "Phylacterine Hive",
            points = 20,
            effect = "Each time the bearer's unit's Reanimation Protocols activate, " +
                "add 1 to the number of wounds reanimated.",
            eligibilityKeywords = listOf("CHARACTER"),
            restrictions = emptyList(),
        ),
        Enhancement(
            id = "nec-enh-soul-circuit",
            detachmentId = "nec-awakened",
            factionId = "necrons",
            name = "Soul Circuitry",
            points = 15,
            effect = "While leading a unit, that unit gains a 5+ invulnerable save.",
            eligibilityKeywords = listOf("CHARACTER"),
            restrictions = emptyList(),
        ),
    )

    private val awakenedDynastyStratagems = listOf(
        Stratagem(
            id = "nec-strat-extermination-protocols",
            factionId = "necrons",
            detachmentId = "nec-awakened",
            name = "Extermination Protocols",
            cp = 1,
            type = StratagemType.BATTLE_TACTIC,
            phase = GamePhase.SHOOTING,
            target = "One NECRONS unit from your army.",
            effect = "Until the end of the phase, ranged weapons in this unit have " +
                "[SUSTAINED HITS 1].",
            restrictions = "",
        ),
        Stratagem(
            id = "nec-strat-protocol-of-undying-legions",
            factionId = "necrons",
            detachmentId = "nec-awakened",
            name = "Protocol of the Undying Legions",
            cp = 2,
            type = StratagemType.STRATEGIC_PLOY,
            phase = GamePhase.COMMAND,
            target = "One NECRONS unit from your army.",
            effect = "The unit's Reanimation Protocols activate twice this turn.",
            restrictions = "",
        ),
    )

    val necDetachments = listOf(
        Detachment(
            id = "nec-awakened",
            factionId = "necrons",
            name = "Awakened Dynasty",
            rule = DetachmentRule(
                name = "My Will Be Done",
                effect = "In your Command phase, you can pick one NECRONS CHARACTER " +
                    "from your army. Until the start of your next Command phase, " +
                    "their unit can re-roll one hit, wound, or damage roll per turn.",
            ),
            enhancements = awakenedDynastyEnhancements,
            stratagems = awakenedDynastyStratagems,
        ),
    )

    val necUnits = listOf(
        Unit(
            id = "nec-overlord",
            factionId = "necrons",
            name = "Overlord",
            role = BattlefieldRole.CHARACTER,
            keywords = listOf("CHARACTER", "INFANTRY", "OVERLORD", "NECRON"),
            factionKeywords = listOf("NECRONS"),
            stats = UnitStats("5\"", 6, "3+", "4+", 5, "6", 1),
            modelGroups = listOf(ModelGroup("Overlord", 1, 1)),
            weapons = listOf(
                WeaponProfile(
                    id = "wpn-staff-of-light-r",
                    name = "Staff of light",
                    type = WeaponType.RANGED,
                    range = "18\"",
                    attacks = "3",
                    skill = "3+",
                    strength = "5",
                    ap = "-2",
                    damage = "2",
                    keywords = listOf("ASSAULT"),
                    abilities = emptyList(),
                ),
                WeaponProfile(
                    id = "wpn-staff-of-light-m",
                    name = "Staff of light (melee)",
                    type = WeaponType.MELEE,
                    range = "Melee",
                    attacks = "4",
                    skill = "3+",
                    strength = "5",
                    ap = "-1",
                    damage = "2",
                    keywords = emptyList(),
                    abilities = emptyList(),
                ),
            ),
            abilities = listOf(
                Ability(
                    id = "abl-relentless-overlord",
                    name = "Relentless Overlord",
                    effect = "Once per turn, the bearer's unit can shoot or charge " +
                        "after Falling Back.",
                    type = AbilityType.UNIT,
                ),
            ),
            wargearOptions = emptyList(),
            pointsCosts = listOf(PointsCost(1, 85)),
            leaderAbility = LeaderAbility(
                effect = "Attaches to NECRONS INFANTRY units.",
                attachKeywords = listOf("INFANTRY"),
            ),
            attachmentTargets = listOf("INFANTRY"),
            isNamedCharacter = false,
            maxPerRoster = 0,
            minModels = 1,
            maxModels = 1,
        ),
        Unit(
            id = "nec-cryptek",
            factionId = "necrons",
            name = "Cryptek",
            role = BattlefieldRole.CHARACTER,
            keywords = listOf("CHARACTER", "INFANTRY", "CRYPTEK", "NECRON"),
            factionKeywords = listOf("NECRONS"),
            stats = UnitStats("5\"", 5, "3+", "4+", 4, "6", 1),
            modelGroups = listOf(ModelGroup("Cryptek", 1, 1)),
            weapons = listOf(
                WeaponProfile(
                    id = "wpn-eldritch-lance",
                    name = "Eldritch lance",
                    type = WeaponType.RANGED,
                    range = "36\"",
                    attacks = "2",
                    skill = "3+",
                    strength = "8",
                    ap = "-2",
                    damage = "D3+1",
                    keywords = listOf("HEAVY"),
                    abilities = emptyList(),
                ),
            ),
            abilities = listOf(
                Ability(
                    id = "abl-cryptek-arrays",
                    name = "Cryptek Arrays",
                    effect = "While leading a unit, models in that unit reanimate " +
                        "1 additional wound per Reanimation Protocols activation.",
                    type = AbilityType.UNIT,
                ),
            ),
            wargearOptions = emptyList(),
            pointsCosts = listOf(PointsCost(1, 65)),
            leaderAbility = LeaderAbility(
                effect = "Attaches to NECRONS INFANTRY units.",
                attachKeywords = listOf("INFANTRY"),
            ),
            attachmentTargets = listOf("INFANTRY"),
            isNamedCharacter = false,
            maxPerRoster = 0,
            minModels = 1,
            maxModels = 1,
        ),
        Unit(
            id = "nec-warriors",
            factionId = "necrons",
            name = "Necron Warriors",
            role = BattlefieldRole.BATTLELINE,
            keywords = listOf("INFANTRY", "BATTLELINE", "NECRON"),
            factionKeywords = listOf("NECRONS"),
            stats = UnitStats("5\"", 4, "4+", null, 1, "8", 2),
            modelGroups = listOf(ModelGroup("Necron Warrior", 10, 20)),
            weapons = listOf(
                WeaponProfile(
                    id = "wpn-gauss-flayer",
                    name = "Gauss flayer",
                    type = WeaponType.RANGED,
                    range = "24\"",
                    attacks = "1",
                    skill = "4+",
                    strength = "4",
                    ap = "-1",
                    damage = "1",
                    keywords = listOf("LETHAL HITS"),
                    abilities = emptyList(),
                ),
            ),
            abilities = listOf(
                Ability(
                    id = "abl-reanimation",
                    name = "Reanimation Protocols",
                    effect = "At the start of your Command phase, this unit reanimates " +
                        "D3 wounds; on a 6, reanimates D6 wounds instead.",
                    type = AbilityType.UNIT,
                ),
            ),
            wargearOptions = emptyList(),
            pointsCosts = listOf(
                PointsCost(10, 110),
                PointsCost(20, 220),
            ),
            leaderAbility = null,
            attachmentTargets = emptyList(),
            isNamedCharacter = false,
            maxPerRoster = 0,
            minModels = 10,
            maxModels = 20,
        ),
        Unit(
            id = "nec-immortals",
            factionId = "necrons",
            name = "Immortals",
            role = BattlefieldRole.INFANTRY,
            keywords = listOf("INFANTRY", "NECRON", "IMMORTALS"),
            factionKeywords = listOf("NECRONS"),
            stats = UnitStats("5\"", 5, "3+", null, 1, "8", 2),
            modelGroups = listOf(ModelGroup("Immortal", 5, 10)),
            weapons = listOf(
                WeaponProfile(
                    id = "wpn-tesla-carbine",
                    name = "Tesla carbine",
                    type = WeaponType.RANGED,
                    range = "24\"",
                    attacks = "2",
                    skill = "4+",
                    strength = "5",
                    ap = "0",
                    damage = "1",
                    keywords = listOf("ASSAULT", "SUSTAINED HITS 2"),
                    abilities = emptyList(),
                ),
            ),
            abilities = listOf(
                Ability(
                    id = "abl-reanimation-imm",
                    name = "Reanimation Protocols",
                    effect = "At the start of your Command phase, reanimate D3 wounds.",
                    type = AbilityType.UNIT,
                ),
            ),
            wargearOptions = emptyList(),
            pointsCosts = listOf(
                PointsCost(5, 80),
                PointsCost(10, 160),
            ),
            leaderAbility = null,
            attachmentTargets = emptyList(),
            isNamedCharacter = false,
            maxPerRoster = 0,
            minModels = 5,
            maxModels = 10,
        ),
    )

    // ───────────────────────── Core stratagems ─────────────────────────

    val coreStratagems = listOf(
        Stratagem(
            id = "core-strat-command-reroll",
            factionId = null,
            detachmentId = null,
            name = "Command Re-roll",
            cp = 1,
            type = StratagemType.STRATEGIC_PLOY,
            phase = GamePhase.ANY,
            target = "Any roll your army makes.",
            effect = "Re-roll one hit, wound, save, damage, or Battle-shock test.",
            restrictions = "",
        ),
        Stratagem(
            id = "core-strat-counter-offensive",
            factionId = null,
            detachmentId = null,
            name = "Counter-offensive",
            cp = 2,
            type = StratagemType.BATTLE_TACTIC,
            phase = GamePhase.FIGHT,
            target = "One unit from your army within Engagement Range of enemy units.",
            effect = "Your unit fights next, even though it is not your turn.",
            restrictions = "",
        ),
        Stratagem(
            id = "core-strat-insane-bravery",
            factionId = null,
            detachmentId = null,
            name = "Insane Bravery",
            cp = 1,
            type = StratagemType.STRATEGIC_PLOY,
            phase = GamePhase.COMMAND,
            target = "One unit from your army that is about to take a Battle-shock test.",
            effect = "That unit automatically passes the Battle-shock test.",
            restrictions = "Use only once per battle.",
        ),
        Stratagem(
            id = "core-strat-tank-shock",
            factionId = null,
            detachmentId = null,
            name = "Tank Shock",
            cp = 1,
            type = StratagemType.BATTLE_TACTIC,
            phase = GamePhase.CHARGE,
            target = "One VEHICLE unit from your army that just made a charge move.",
            effect = "Inflict mortal wounds equal to D3 plus the unit's Toughness " +
                "characteristic on a target unit.",
            restrictions = "",
        ),
    )

    // ───────────────────────── Aggregated ─────────────────────────

    val allDetachments: List<Detachment> get() = smDetachments + necDetachments
    val allUnits: List<Unit> get() = smUnits + necUnits
    val allEnhancements: List<Enhancement> get() = allDetachments.flatMap { it.enhancements }
    val allStratagems: List<Stratagem> get() =
        coreStratagems + allDetachments.flatMap { it.stratagems }
}
