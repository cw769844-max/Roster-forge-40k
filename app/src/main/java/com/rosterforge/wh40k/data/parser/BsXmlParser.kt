package com.rosterforge.wh40k.data.parser

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
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.io.InputStream
import java.util.zip.ZipInputStream
import javax.inject.Inject
import javax.inject.Singleton
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Parser for BattleScribe wh40k-10e data files.
 *
 * Pipeline:
 *  1. [parseRelease] walks the ZIP, calls [parseGameSystem] for the single
 *     `.gst` and [parseCatalogue] for each `.cat`.
 *  2. Each per-file parser reads the entire stream into an in-memory
 *     [BsNode] tree, then traverses it to extract domain objects.
 *  3. The game-system pass also returns an index of shared entries by id,
 *     which catalogue passes consult when resolving `<entryLink>` nodes.
 *  4. Aggregated results are deduplicated by id (BSData re-publishes the
 *     same shared unit through entry-links across catalogues).
 */
@Singleton
class BsXmlParser @Inject constructor() {

    private val docBuilderFactory: DocumentBuilderFactory by lazy {
        DocumentBuilderFactory.newInstance().apply {
            isNamespaceAware = false
            isValidating = false
            setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
        }
    }

    fun parseRelease(zip: InputStream): BsParserResult {
        val warnings = mutableListOf<String>()
        val factions = mutableListOf<Faction>()
        val detachments = mutableListOf<Detachment>()
        val units = mutableListOf<Unit>()
        val enhancements = mutableListOf<Enhancement>()
        val stratagems = mutableListOf<Stratagem>()
        var sharedEntries: Map<String, BsNode> = emptyMap()

        // First pass: collect every entry into memory so we can read the .gst
        // before any .cat (ZipInputStream is forward-only, but BSData puts the
        // .gst at the top of the archive — still, buffer for safety).
        val entries = mutableListOf<Pair<String, ByteArray>>()
        ZipInputStream(zip).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                if (!entry.isDirectory) {
                    entries += entry.name to zis.readBytes()
                }
                entry = zis.nextEntry
            }
        }

        // Game system first so shared entries are indexed.
        entries.firstOrNull { it.first.endsWith(".gst", ignoreCase = true) }
            ?.let { (name, bytes) ->
                runCatching { parseGameSystem(bytes.inputStream()) }
                    .onSuccess { gs ->
                        stratagems += gs.coreStratagems
                        sharedEntries = gs.sharedEntries
                    }
                    .onFailure { warnings += "Failed to parse $name: ${it.message}" }
            }

        for ((name, bytes) in entries) {
            if (!name.endsWith(".cat", ignoreCase = true)) continue
            runCatching { parseCatalogue(bytes.inputStream(), sharedEntries) }
                .onSuccess { cat ->
                    cat.faction?.let { factions += it }
                    detachments += cat.detachments
                    units += cat.units
                    enhancements += cat.enhancements
                    stratagems += cat.stratagems
                }
                .onFailure { warnings += "Failed to parse $name: ${it.message}" }
        }

        return BsParserResult(
            factions = factions.distinctBy { it.id },
            detachments = detachments.distinctBy { it.id },
            units = units.distinctBy { it.id },
            enhancements = enhancements.distinctBy { it.id },
            stratagems = stratagems.distinctBy { it.id },
            warnings = warnings,
        )
    }

    fun parseGameSystem(input: InputStream): GameSystemParseResult =
        parseGameSystem(input, sharedEntriesOverride = null)

    fun parseCatalogue(input: InputStream): CatalogueParseResult =
        parseCatalogue(input, sharedEntries = emptyMap())

    internal fun parseGameSystem(
        input: InputStream,
        sharedEntriesOverride: Map<String, BsNode>?,
    ): GameSystemParseResult {
        val root = readTree(input) ?: return GameSystemParseResult(emptyList())
        val sharedIdx = mutableMapOf<String, BsNode>()
        for (node in root.walk()) {
            val id = node.attrs["id"] ?: continue
            if (node.tag == "selectionEntry" || node.tag == "selectionEntryGroup" ||
                node.tag == "profile" || node.tag == "rule"
            ) {
                sharedIdx[id] = node
            }
        }
        val effectiveShared = sharedEntriesOverride ?: sharedIdx
        val coreStratagems = topLevelEntries(root)
            .filter { isStratagem(it) }
            .mapNotNull { node -> nodeToStratagem(node, factionId = null, detachmentId = null) }
        return GameSystemParseResult(
            coreStratagems = coreStratagems,
            sharedEntries = effectiveShared,
        )
    }

    internal fun parseCatalogue(
        input: InputStream,
        sharedEntries: Map<String, BsNode>,
    ): CatalogueParseResult {
        val root = readTree(input) ?: return EMPTY_CATALOGUE
        if (root.tag != "catalogue") return EMPTY_CATALOGUE

        val factionId = root.attrs["id"] ?: return EMPTY_CATALOGUE
        val factionName = root.attrs["name"].orEmpty()
        val faction = Faction(
            id = factionId,
            name = factionName,
            abbreviation = factionName.split(' ')
                .filter { it.isNotEmpty() }
                .joinToString("") { it.first().uppercase() }
                .take(4),
            factionKeyword = factionName.uppercase(),
            subFactions = emptyList(),
            allegiance = inferAllegiance(factionId, factionName),
        )

        val localShared = root.walk()
            .filter { (it.tag == "selectionEntry" || it.tag == "profile") && it.attrs["id"] != null }
            .associateBy { it.attrs["id"]!! }
        val combinedShared = sharedEntries + localShared

        val units = mutableListOf<Unit>()
        val stratagems = mutableListOf<Stratagem>()
        val detachments = mutableListOf<Detachment>()
        val enhancements = mutableListOf<Enhancement>()

        for (node in topLevelEntries(root)) {
            when {
                isDetachment(node) -> {
                    val det = nodeToDetachment(node, factionId, combinedShared)
                    if (det != null) {
                        detachments += det
                        enhancements += det.enhancements
                        stratagems += det.stratagems
                    }
                }
                isStratagem(node) ->
                    nodeToStratagem(node, factionId, detachmentId = null)?.let(stratagems::add)
                isUnit(node) -> nodeToUnit(node, factionId, combinedShared)?.let(units::add)
            }
        }

        return CatalogueParseResult(
            faction = faction,
            detachments = detachments,
            units = units,
            enhancements = enhancements,
            stratagems = stratagems,
        )
    }

    // ─────────────────────────── tree reading ───────────────────────────

    private fun readTree(input: InputStream): BsNode? {
        val builder = docBuilderFactory.newDocumentBuilder()
        val doc = builder.parse(input)
        val root = doc.documentElement ?: return null
        return readElement(root)
    }

    private fun readElement(element: Element): BsNode {
        val attrs = mutableMapOf<String, String>()
        val attributeMap = element.attributes
        for (i in 0 until attributeMap.length) {
            val a = attributeMap.item(i)
            attrs[a.nodeName] = a.nodeValue ?: ""
        }
        val children = mutableListOf<BsNode>()
        val text = StringBuilder()
        val nodeList = element.childNodes
        for (i in 0 until nodeList.length) {
            val n = nodeList.item(i)
            when (n.nodeType) {
                Node.ELEMENT_NODE -> children += readElement(n as Element)
                Node.TEXT_NODE, Node.CDATA_SECTION_NODE -> n.nodeValue?.let { text.append(it) }
            }
        }
        return BsNode(
            tag = element.nodeName,
            attrs = attrs,
            children = children,
            text = text.toString().trim(),
        )
    }

    /**
     * Returns the selectionEntry nodes that should be treated as top-level
     * (i.e. potential units, stratagems, or detachments). Excludes nested
     * model variants and wargear-options inside a unit.
     */
    private fun topLevelEntries(root: BsNode): List<BsNode> =
        (root.childrenWithTag("selectionEntries") +
            root.childrenWithTag("sharedSelectionEntries"))
            .flatMap { it.childrenWithTag("selectionEntry") }

    // ─────────────────────────── classifiers ───────────────────────────

    private fun categoryNames(node: BsNode): Set<String> =
        node.childrenWithTag("categoryLinks")
            .flatMap { it.childrenWithTag("categoryLink") }
            .mapNotNull { it.attrs["name"] }
            .toSet()

    private fun isUnit(node: BsNode): Boolean {
        if (node.attrs["type"] == "unit") return true
        if (node.attrs["type"] == "model") return true
        // Some entries omit type but carry a Unit profile.
        return profilesOfType(node, "Unit").isNotEmpty()
    }

    private fun isStratagem(node: BsNode): Boolean =
        categoryNames(node).any { it.equals("Stratagem", ignoreCase = true) } ||
            profilesOfType(node, "Stratagem").isNotEmpty()

    private fun isDetachment(node: BsNode): Boolean =
        categoryNames(node).any { it.equals("Detachment", ignoreCase = true) } ||
            node.attrs["name"]?.contains("Detachment", ignoreCase = true) == true &&
            profilesOfType(node, "Detachment Rule").isNotEmpty()

    private fun isEnhancement(node: BsNode): Boolean =
        categoryNames(node).any { it.equals("Enhancement", ignoreCase = true) } ||
            profilesOfType(node, "Enhancement").isNotEmpty()

    private fun profilesOfType(node: BsNode, typeName: String): List<BsNode> =
        node.childrenWithTag("profiles")
            .flatMap { it.childrenWithTag("profile") }
            .filter { it.attrs["typeName"]?.equals(typeName, ignoreCase = true) == true }

    private fun characteristic(profile: BsNode, name: String): String? =
        profile.childrenWithTag("characteristics")
            .flatMap { it.childrenWithTag("characteristic") }
            .firstOrNull { it.attrs["name"].equals(name, ignoreCase = true) }
            ?.text
            ?.takeIf { it.isNotBlank() }

    private fun cost(node: BsNode, name: String): Int? =
        node.childrenWithTag("costs")
            .flatMap { it.childrenWithTag("cost") }
            .firstOrNull { it.attrs["name"].equals(name, ignoreCase = true) }
            ?.attrs?.get("value")
            ?.toDoubleOrNull()
            ?.toInt()

    // ─────────────────────────── domain extractors ───────────────────────────

    private fun nodeToUnit(
        entry: BsNode,
        factionId: String,
        shared: Map<String, BsNode>,
    ): Unit? {
        val id = entry.attrs["id"] ?: return null
        val name = entry.attrs["name"] ?: return null
        val cats = categoryNames(entry)
        val role = cats.asSequence()
            .mapNotNull { mapBattlefieldRole(it) }
            .firstOrNull() ?: BattlefieldRole.OTHER

        val unitProfile = profilesOfType(entry, "Unit").firstOrNull()
        val stats = unitProfile?.let {
            UnitStats(
                movement = characteristic(it, "M") ?: "",
                toughness = characteristic(it, "T")?.toIntOrNull() ?: 0,
                save = characteristic(it, "Sv") ?: "",
                invulnerableSave = profilesOfType(entry, "Invulnerable Save")
                    .firstOrNull()?.let { p -> characteristic(p, "Inv") ?: characteristic(p, "Save") },
                wounds = characteristic(it, "W")?.toIntOrNull() ?: 1,
                leadership = characteristic(it, "Ld") ?: "",
                objectiveControl = characteristic(it, "OC")?.toIntOrNull() ?: 0,
            )
        } ?: UnitStats("", 0, "", null, 1, "", 0)

        val rangedWeapons = profilesOfType(entry, "Ranged Weapons")
            .map { weaponFromProfile(it, WeaponType.RANGED) }
        val meleeWeapons = profilesOfType(entry, "Melee Weapons")
            .map { weaponFromProfile(it, WeaponType.MELEE) }
        val abilities = profilesOfType(entry, "Abilities")
            .map { abilityFromProfile(it) }

        val pointsCosts = listOfNotNull(
            cost(entry, "pts")?.let { PointsCost(modelCount = 1, points = it) }
        ).ifEmpty {
            entry.children
                .mapNotNull { c -> cost(c, "pts")?.let { PointsCost(modelCount = 1, points = it) } }
        }

        val isNamed = cats.any { it.equals("Epic Hero", ignoreCase = true) }
        val leaderProfile = profilesOfType(entry, "Leader").firstOrNull()
        val leaderAbility = leaderProfile?.let {
            LeaderAbility(
                effect = characteristic(it, "Description")
                    ?: characteristic(it, "Effect")
                    ?: "",
                attachKeywords = (characteristic(it, "Attached Unit")
                    ?: characteristic(it, "Attached Units")
                    ?: "")
                    .split(',', '\n', ';')
                    .map(String::trim)
                    .filter { kw -> kw.isNotEmpty() },
            )
        }

        val modelGroups = entry.walk()
            .filter { it.tag == "selectionEntry" && it.attrs["type"] == "model" }
            .map { m ->
                ModelGroup(
                    name = m.attrs["name"] ?: "Model",
                    minCount = m.childrenWithTag("constraints")
                        .flatMap { it.childrenWithTag("constraint") }
                        .firstOrNull { it.attrs["type"] == "min" }
                        ?.attrs?.get("value")?.toDoubleOrNull()?.toInt() ?: 1,
                    maxCount = m.childrenWithTag("constraints")
                        .flatMap { it.childrenWithTag("constraint") }
                        .firstOrNull { it.attrs["type"] == "max" }
                        ?.attrs?.get("value")?.toDoubleOrNull()?.toInt() ?: 1,
                )
            }
            .toList()
            .ifEmpty { listOf(ModelGroup(name, 1, 1)) }

        val totalMin = modelGroups.sumOf { it.minCount }
        val totalMax = modelGroups.sumOf { it.maxCount }

        return Unit(
            id = id,
            factionId = factionId,
            name = name,
            role = role,
            keywords = cats.toList(),
            factionKeywords = listOfNotNull(cats.firstOrNull { it.equals("Faction", true) }),
            stats = stats,
            modelGroups = modelGroups,
            weapons = rangedWeapons + meleeWeapons,
            abilities = abilities,
            wargearOptions = wargearOptionsFor(entry, shared),
            pointsCosts = pointsCosts.ifEmpty { listOf(PointsCost(1, 0)) },
            leaderAbility = leaderAbility,
            attachmentTargets = leaderAbility?.attachKeywords.orEmpty(),
            isNamedCharacter = isNamed,
            maxPerRoster = if (isNamed) 1 else 0,
            minModels = totalMin,
            maxModels = totalMax,
        )
    }

    private fun weaponFromProfile(p: BsNode, type: WeaponType): WeaponProfile = WeaponProfile(
        id = p.attrs["id"] ?: ("wpn-" + (p.attrs["name"] ?: "unknown")),
        name = p.attrs["name"] ?: "",
        type = type,
        range = characteristic(p, "Range") ?: if (type == WeaponType.MELEE) "Melee" else "",
        attacks = characteristic(p, "A") ?: "",
        skill = characteristic(p, if (type == WeaponType.MELEE) "WS" else "BS") ?: "",
        strength = characteristic(p, "S") ?: "",
        ap = characteristic(p, "AP") ?: "",
        damage = characteristic(p, "D") ?: "",
        keywords = (characteristic(p, "Keywords") ?: "")
            .split(',').map(String::trim).filter { it.isNotEmpty() && it != "-" },
        abilities = emptyList(),
    )

    private fun abilityFromProfile(p: BsNode): Ability = Ability(
        id = p.attrs["id"] ?: ("abl-" + (p.attrs["name"] ?: "unknown")),
        name = p.attrs["name"] ?: "",
        effect = characteristic(p, "Description") ?: characteristic(p, "Effect") ?: "",
        type = AbilityType.UNIT,
        phase = null,
    )

    private fun wargearOptionsFor(entry: BsNode, shared: Map<String, BsNode>): List<WargearOption> {
        val groups = entry.walk()
            .filter { it.tag == "selectionEntryGroup" && it.attrs["id"] != null }
            .toList()
        return groups.map { group ->
            val constraints = group.childrenWithTag("constraints")
                .flatMap { it.childrenWithTag("constraint") }
            val minSel = constraints.firstOrNull { it.attrs["type"] == "min" }
                ?.attrs?.get("value")?.toDoubleOrNull()?.toInt() ?: 0
            val maxSel = constraints.firstOrNull { it.attrs["type"] == "max" }
                ?.attrs?.get("value")?.toDoubleOrNull()?.toInt() ?: 1
            val choices = (group.childrenWithTag("selectionEntries")
                .flatMap { it.childrenWithTag("selectionEntry") } +
                group.childrenWithTag("entryLinks")
                    .flatMap { it.childrenWithTag("entryLink") }
                    .mapNotNull { link -> shared[link.attrs["targetId"]] })
                .map { choiceNode ->
                    WargearChoice(
                        id = choiceNode.attrs["id"] ?: "",
                        name = choiceNode.attrs["name"] ?: "",
                        pointsCost = cost(choiceNode, "pts") ?: 0,
                    )
                }
            WargearOption(
                id = group.attrs["id"]!!,
                description = group.attrs["name"] ?: "",
                constraints = WargearConstraints(
                    minSelections = minSel,
                    maxSelections = maxSel,
                    modelScope = ModelScope.PER_UNIT,
                ),
                choices = choices,
            )
        }
    }

    private fun nodeToStratagem(
        entry: BsNode,
        factionId: String?,
        detachmentId: String?,
    ): Stratagem? {
        val id = entry.attrs["id"] ?: return null
        val name = entry.attrs["name"] ?: return null
        val profile = profilesOfType(entry, "Stratagem").firstOrNull()
        val cp = cost(entry, "CP")
            ?: profile?.let { characteristic(it, "CP")?.toIntOrNull() }
            ?: 1
        val phaseText = profile?.let { characteristic(it, "When") }
            ?: profile?.let { characteristic(it, "Phase") }
            ?: ""
        return Stratagem(
            id = id,
            factionId = factionId,
            detachmentId = detachmentId,
            name = name,
            cp = cp,
            type = mapStratagemType(profile?.let { characteristic(it, "Type") } ?: ""),
            phase = mapPhase(phaseText),
            target = profile?.let { characteristic(it, "Target") } ?: "",
            effect = profile?.let { characteristic(it, "Effect") }
                ?: profile?.let { characteristic(it, "Description") }
                ?: "",
            restrictions = profile?.let { characteristic(it, "Restrictions") } ?: "",
            flavor = null,
        )
    }

    private fun nodeToDetachment(
        entry: BsNode,
        factionId: String,
        shared: Map<String, BsNode>,
    ): Detachment? {
        val id = entry.attrs["id"] ?: return null
        val name = entry.attrs["name"] ?: return null
        val ruleProfile = profilesOfType(entry, "Detachment Rule").firstOrNull()
            ?: profilesOfType(entry, "Detachment").firstOrNull()
        val rule = DetachmentRule(
            name = ruleProfile?.attrs?.get("name") ?: name,
            effect = ruleProfile?.let { characteristic(it, "Description") }
                ?: ruleProfile?.let { characteristic(it, "Effect") }
                ?: "",
        )

        val nestedEnhancements = entry.walk()
            .filter { it.tag == "selectionEntry" && isEnhancement(it) }
            .mapNotNull { nodeToEnhancement(it, factionId, id) }
            .toList()

        val nestedStratagems = entry.walk()
            .filter { it.tag == "selectionEntry" && isStratagem(it) }
            .mapNotNull { nodeToStratagem(it, factionId, id) }
            .toList()

        return Detachment(
            id = id,
            factionId = factionId,
            name = name,
            rule = rule,
            enhancements = nestedEnhancements,
            stratagems = nestedStratagems,
        )
    }

    private fun nodeToEnhancement(
        entry: BsNode,
        factionId: String,
        detachmentId: String,
    ): Enhancement? {
        val id = entry.attrs["id"] ?: return null
        val name = entry.attrs["name"] ?: return null
        val profile = profilesOfType(entry, "Enhancement").firstOrNull()
        val effect = profile?.let { characteristic(it, "Description") }
            ?: profile?.let { characteristic(it, "Effect") }
            ?: ""
        val keywords = (profile?.let { characteristic(it, "Restrictions") }
            ?: profile?.let { characteristic(it, "Keywords") }
            ?: "")
            .split(',').map(String::trim).filter { it.isNotEmpty() }
        return Enhancement(
            id = id,
            detachmentId = detachmentId,
            factionId = factionId,
            name = name,
            points = cost(entry, "pts") ?: 0,
            effect = effect,
            eligibilityKeywords = keywords,
            restrictions = emptyList(),
        )
    }

    // ─────────────────────────── small mappers ───────────────────────────

    private fun mapBattlefieldRole(category: String): BattlefieldRole? =
        when (category.lowercase()) {
            "epic hero" -> BattlefieldRole.EPIC_HERO
            "character" -> BattlefieldRole.CHARACTER
            "battleline" -> BattlefieldRole.BATTLELINE
            "infantry" -> BattlefieldRole.INFANTRY
            "mounted" -> BattlefieldRole.MOUNTED
            "vehicle" -> BattlefieldRole.VEHICLE
            "monster" -> BattlefieldRole.MONSTER
            "fly" -> BattlefieldRole.FLY
            "dedicated transport" -> BattlefieldRole.DEDICATED_TRANSPORT
            "allied units" -> BattlefieldRole.ALLIED_UNITS
            "fortification" -> BattlefieldRole.FORTIFICATION
            else -> null
        }

    private fun mapStratagemType(text: String): StratagemType = when {
        text.contains("battle tactic", ignoreCase = true) -> StratagemType.BATTLE_TACTIC
        text.contains("strategic ploy", ignoreCase = true) -> StratagemType.STRATEGIC_PLOY
        text.contains("epic deed", ignoreCase = true) -> StratagemType.EPIC_DEED
        text.contains("wargear", ignoreCase = true) -> StratagemType.WARGEAR
        text.contains("requisition", ignoreCase = true) -> StratagemType.REQUISITION
        else -> StratagemType.STRATEGIC
    }

    private fun mapPhase(text: String): GamePhase = when {
        text.contains("command", ignoreCase = true) -> GamePhase.COMMAND
        text.contains("movement", ignoreCase = true) -> GamePhase.MOVEMENT
        text.contains("shooting", ignoreCase = true) -> GamePhase.SHOOTING
        text.contains("charge", ignoreCase = true) -> GamePhase.CHARGE
        text.contains("fight", ignoreCase = true) -> GamePhase.FIGHT
        text.contains("morale", ignoreCase = true) -> GamePhase.MORALE
        else -> GamePhase.ANY
    }

    private fun inferAllegiance(id: String, name: String): Allegiance {
        val key = (id + " " + name).lowercase()
        return when {
            "imperium" in key || "adeptus" in key || "imperial" in key ||
                "astra militarum" in key || "grey knights" in key ||
                "sisters" in key || "custodes" in key -> Allegiance.IMPERIUM
            "chaos" in key || "death guard" in key || "world eaters" in key ||
                "thousand sons" in key -> Allegiance.CHAOS
            "necron" in key || "tau" in key || "t'au" in key || "tyranid" in key ||
                "aeldari" in key || "drukhari" in key || "ork" in key ||
                "genestealer" in key || "leagues" in key || "votann" in key -> Allegiance.XENOS
            else -> Allegiance.UNALIGNED
        }
    }

    private companion object {
        val EMPTY_CATALOGUE = CatalogueParseResult(
            faction = null,
            detachments = emptyList(),
            units = emptyList(),
            enhancements = emptyList(),
            stratagems = emptyList(),
        )
    }
}
