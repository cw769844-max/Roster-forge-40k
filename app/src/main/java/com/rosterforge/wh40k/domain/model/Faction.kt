package com.rosterforge.wh40k.domain.model

data class Faction(
    val id: String,
    val name: String,
    val abbreviation: String,
    val factionKeyword: String,
    val subFactions: List<String>,
    val allegiance: Allegiance,
)
