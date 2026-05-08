package com.rosterforge.wh40k.data.parser

import com.rosterforge.wh40k.domain.model.Detachment
import com.rosterforge.wh40k.domain.model.Enhancement
import com.rosterforge.wh40k.domain.model.Faction
import com.rosterforge.wh40k.domain.model.Stratagem
import com.rosterforge.wh40k.domain.model.Unit

/** Aggregated parser output ready to be inserted into the local database. */
data class BsParserResult(
    val factions: List<Faction>,
    val detachments: List<Detachment>,
    val units: List<Unit>,
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
