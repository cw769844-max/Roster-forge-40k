package com.rosterforge.wh40k.domain.usecase

import com.rosterforge.wh40k.domain.model.Roster
import com.rosterforge.wh40k.domain.repository.CatalogueRepository
import com.rosterforge.wh40k.domain.repository.RosterRepository
import java.util.UUID
import javax.inject.Inject

class CreateRosterUseCase @Inject constructor(
    private val rosterRepository: RosterRepository,
    private val catalogueRepository: CatalogueRepository,
) {
    suspend operator fun invoke(
        name: String,
        pointsLimit: Int,
        factionId: String,
        detachmentId: String,
    ): Result<String> = runCatching {
        require(name.isNotBlank()) { "Roster name cannot be blank" }
        require(pointsLimit in 0..5000) { "Invalid points limit" }
        val faction = catalogueRepository.getFaction(factionId)
            ?: error("Unknown faction $factionId")
        val detachment = catalogueRepository.getDetachment(detachmentId)
            ?: error("Unknown detachment $detachmentId")
        require(detachment.factionId == factionId) {
            "Detachment ${detachment.name} does not belong to faction ${faction.name}"
        }
        val now = System.currentTimeMillis()
        val roster = Roster(
            id = UUID.randomUUID().toString(),
            name = name.trim(),
            factionId = faction.id,
            factionName = faction.name,
            detachmentId = detachment.id,
            detachmentName = detachment.name,
            pointsLimit = pointsLimit,
            units = emptyList(),
            createdAt = now,
            updatedAt = now,
        )
        rosterRepository.upsertRoster(roster)
        roster.id
    }
}
