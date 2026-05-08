package com.rosterforge.wh40k.data.parser

import com.rosterforge.wh40k.domain.model.Detachment
import com.rosterforge.wh40k.domain.model.Enhancement
import com.rosterforge.wh40k.domain.model.Faction
import com.rosterforge.wh40k.domain.model.Stratagem
import com.rosterforge.wh40k.domain.model.Unit as DomainUnit

/** Aggregated parser output ready to be inserted into the local database. */
data class BsParserResult(
    val factions: List<Faction>,
    val detachments: List<Detachment>,
    val units: List<DomainUnit>,
    val enhancements: List<Enhancement>,
    val stratagems: List<Stratagem>,
    val warnings: List<String>,
) {
    companion object {
        val Empty = BsParserResult(
            factions = emptyList(),
            detachments = emptyList(),
            units = emptyList(),
            enhancements = emptyList(),
            stratagems = emptyList(),
            warnings = emptyList(),
        )
    }
}

/**
 * In-memory representation of a parsed BattleScribe XML element. The whole
 * `.gst`/`.cat` file is loaded into a tree of these nodes before extraction,
 * which keeps the per-file parsing pass linear and the consumers simple.
 */
data class BsNode(
    val tag: String,
    val attrs: Map<String, String>,
    val children: List<BsNode>,
    val text: String,
) {
    fun child(tag: String): BsNode? = children.firstOrNull { it.tag == tag }
    fun childrenWithTag(tag: String): List<BsNode> = children.filter { it.tag == tag }

    /** Recursively flatten every descendant (including this node). */
    fun walk(): Sequence<BsNode> = sequence {
        yield(this@BsNode)
        children.forEach { yieldAll(it.walk()) }
    }
}

/** Result of parsing a `.gst` file. */
data class GameSystemParseResult(
    val coreStratagems: List<com.rosterforge.wh40k.domain.model.Stratagem>,
    /** id → shared entry node, used by catalogue passes to resolve `<entryLink>`. */
    val sharedEntries: Map<String, BsNode> = emptyMap(),
)

data class CatalogueParseResult(
    val faction: com.rosterforge.wh40k.domain.model.Faction?,
    val detachments: List<com.rosterforge.wh40k.domain.model.Detachment>,
    val units: List<DomainUnit>,
    val enhancements: List<com.rosterforge.wh40k.domain.model.Enhancement>,
    val stratagems: List<com.rosterforge.wh40k.domain.model.Stratagem>,
)
