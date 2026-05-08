package com.rosterforge.wh40k.domain.model

data class Stratagem(
    val id: String,
    val factionId: String?,            // null = core stratagem
    val detachmentId: String?,         // null = faction-wide or core
    val name: String,
    val cp: Int,
    val type: StratagemType,
    val phase: GamePhase,
    val target: String,
    val effect: String,
    val restrictions: String,
    val flavor: String? = null,
)
