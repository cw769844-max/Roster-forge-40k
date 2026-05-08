package com.rosterforge.wh40k.domain.model

data class Detachment(
    val id: String,
    val factionId: String,
    val name: String,
    val rule: DetachmentRule,
    val enhancements: List<Enhancement>,
    val stratagems: List<Stratagem>,
)

data class DetachmentRule(
    val name: String,
    val effect: String,
)

data class Enhancement(
    val id: String,
    val detachmentId: String,
    val factionId: String,
    val name: String,
    val points: Int,
    val effect: String,
    val eligibilityKeywords: List<String>,
    val restrictions: List<String>,
)
