package com.rosterforge.wh40k.domain.usecase

import com.rosterforge.wh40k.domain.repository.CatalogueRepository
import com.rosterforge.wh40k.domain.repository.RosterRepository
import javax.inject.Inject

class AttachLeaderUseCase @Inject constructor(
    private val rosterRepository: RosterRepository,
    private val catalogueRepository: CatalogueRepository,
) {
    /** Pass `leaderRosterUnitId = null` to detach the current leader. */
    suspend operator fun invoke(
        rosterId: String,
        bodyguardRosterUnitId: String,
        leaderRosterUnitId: String?,
    ): Result<Unit> = runCatching {
        val roster = rosterRepository.getRoster(rosterId) ?: error("Roster not found")
        val bodyguard = roster.units.firstOrNull { it.id == bodyguardRosterUnitId }
            ?: error("Bodyguard unit not found")
        require(leaderRosterUnitId != bodyguardRosterUnitId) {
            "A unit cannot be attached to itself."
        }
        if (leaderRosterUnitId != null) {
            val leader = roster.units.firstOrNull { it.id == leaderRosterUnitId }
                ?: error("Leader unit not in roster")
            val snapshot = catalogueRepository
                .getCatalogueSnapshot(roster.factionId, roster.detachmentId)
                ?: error("Catalogue unavailable")
            val leaderCatalogue = snapshot.unitsById[leader.unitId]
                ?: error("Leader catalogue entry missing")
            val bodyguardCatalogue = snapshot.unitsById[bodyguard.unitId]
                ?: error("Bodyguard catalogue entry missing")
            val attachKeywords = leaderCatalogue.leaderAbility?.attachKeywords
                ?: leaderCatalogue.attachmentTargets
            if (attachKeywords.isNotEmpty()) {
                val bodyguardKw =
                    (bodyguardCatalogue.keywords + bodyguardCatalogue.factionKeywords).toSet()
                val missing = attachKeywords.filter { it !in bodyguardKw }
                require(missing.isEmpty()) {
                    "${leaderCatalogue.name} cannot lead ${bodyguardCatalogue.name}: " +
                        "missing keyword(s) ${missing.joinToString()}"
                }
            }
            // Ensure leader is not already attached elsewhere.
            roster.units.filter {
                it.id != bodyguardRosterUnitId &&
                    it.attachedLeaderRosterUnitId == leaderRosterUnitId
            }.forEach { other ->
                rosterRepository.upsertRosterUnit(
                    other.copy(attachedLeaderRosterUnitId = null),
                )
            }
        }
        rosterRepository.upsertRosterUnit(
            bodyguard.copy(attachedLeaderRosterUnitId = leaderRosterUnitId),
        )
    }
}
