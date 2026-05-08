package com.rosterforge.wh40k.data.parser

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.InputStream
import java.util.zip.ZipInputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Parser for BattleScribe wh40k-10e data files.
 *
 * The on-disk format is one of:
 *  - A `.zip` archive containing many `.cat` and one `.gst` file.
 *  - A bare `.cat` / `.gst` file (also XML).
 *
 * The high-level pipeline is:
 *   1. [parseRelease] — entry point. Walks a ZIP archive entry-by-entry.
 *   2. [parseGameSystem] — extracts core/global stratagems, profile types,
 *      and shared rules from the `.gst` file.
 *   3. [parseCatalogue] — extracts a single faction's units, detachments,
 *      enhancements, and faction-specific stratagems from a `.cat` file.
 *   4. [linkResolver] (TODO) — resolves `<entryLink>` references between
 *      catalogues and shared entries from the game system.
 *
 * This implementation is intentionally a skeleton: the heavy lifting of
 * reading every BSData element happens in subclasses split per element type.
 * The MVP target is to wire the structural traversal end-to-end; element-level
 * parsing is filled in incrementally as factions are imported and tested.
 */
@Singleton
class BsXmlParser @Inject constructor() {

    private val factory: XmlPullParserFactory by lazy {
        XmlPullParserFactory.newInstance().apply {
            isNamespaceAware = false
        }
    }

    /**
     * Parse a complete BSData release (ZIP archive). Each contained `.gst`
     * and `.cat` file is parsed independently, then results are combined and
     * returned as a single [BsParserResult].
     */
    fun parseRelease(zip: InputStream): BsParserResult {
        val warnings = mutableListOf<String>()
        val factions = mutableListOf<com.rosterforge.wh40k.domain.model.Faction>()
        val detachments = mutableListOf<com.rosterforge.wh40k.domain.model.Detachment>()
        val units = mutableListOf<com.rosterforge.wh40k.domain.model.Unit>()
        val enhancements = mutableListOf<com.rosterforge.wh40k.domain.model.Enhancement>()
        val stratagems = mutableListOf<com.rosterforge.wh40k.domain.model.Stratagem>()

        ZipInputStream(zip).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                val current = entry
                runCatching {
                    when {
                        current.name.endsWith(".gst", ignoreCase = true) -> {
                            val gs = parseGameSystem(zis)
                            stratagems += gs.coreStratagems
                        }
                        current.name.endsWith(".cat", ignoreCase = true) -> {
                            val cat = parseCatalogue(zis)
                            cat.faction?.let { factions += it }
                            detachments += cat.detachments
                            units += cat.units
                            enhancements += cat.enhancements
                            stratagems += cat.stratagems
                        }
                    }
                }.onFailure { cause ->
                    warnings += "Failed to parse ${current.name}: ${cause.message}"
                }
                entry = zis.nextEntry
            }
        }
        return BsParserResult(
            factions = factions,
            detachments = detachments,
            units = units,
            enhancements = enhancements,
            stratagems = stratagems,
            warnings = warnings,
        )
    }

    fun parseGameSystem(input: InputStream): GameSystemParseResult {
        val parser = factory.newPullParser().apply { setInput(input, "UTF-8") }
        val coreStratagems = mutableListOf<com.rosterforge.wh40k.domain.model.Stratagem>()
        // TODO: walk <gameSystem> tree; extract <selectionEntry>s with type "Stratagem"
        //  whose category indicates "Core Stratagem" and convert to domain Stratagem
        //  with factionId = null, detachmentId = null.
        consume(parser)
        return GameSystemParseResult(coreStratagems = coreStratagems)
    }

    fun parseCatalogue(input: InputStream): CatalogueParseResult {
        val parser = factory.newPullParser().apply { setInput(input, "UTF-8") }
        // TODO:
        //  1. Read root <catalogue id name>; emit Faction with derived keyword.
        //  2. Walk <selectionEntries> at the top level. For each entry of
        //     type "unit", build a domain Unit. For "upgrade" entries that
        //     match the Detachment category, build a Detachment + nested
        //     Enhancements.
        //  3. Stratagem entries become Stratagem domain objects scoped to
        //     this faction (and detachment if nested under one).
        //  4. Resolve <entryLink target=…> nodes by recording references and
        //     replaying them in a second pass against shared entries.
        consume(parser)
        return CatalogueParseResult(
            faction = null,
            detachments = emptyList(),
            units = emptyList(),
            enhancements = emptyList(),
            stratagems = emptyList(),
        )
    }

    /** Stub that consumes the entire stream so Android does not complain about
     *  un-closed parser handles. Replace with the real recursive descent later. */
    private fun consume(parser: XmlPullParser) {
        var event = parser.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            event = parser.next()
        }
    }
}

data class GameSystemParseResult(
    val coreStratagems: List<com.rosterforge.wh40k.domain.model.Stratagem>,
)

data class CatalogueParseResult(
    val faction: com.rosterforge.wh40k.domain.model.Faction?,
    val detachments: List<com.rosterforge.wh40k.domain.model.Detachment>,
    val units: List<com.rosterforge.wh40k.domain.model.Unit>,
    val enhancements: List<com.rosterforge.wh40k.domain.model.Enhancement>,
    val stratagems: List<com.rosterforge.wh40k.domain.model.Stratagem>,
)
