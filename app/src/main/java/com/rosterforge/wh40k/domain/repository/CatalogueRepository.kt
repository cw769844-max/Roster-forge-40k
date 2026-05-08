package com.rosterforge.wh40k.domain.repository

import com.rosterforge.wh40k.domain.model.CatalogueSnapshot
import com.rosterforge.wh40k.domain.model.Detachment
import com.rosterforge.wh40k.domain.model.Faction
import com.rosterforge.wh40k.domain.model.Unit

interface CatalogueRepository {
    suspend fun getAllFactions(): List<Faction>
    suspend fun getFaction(id: String): Faction?
    suspend fun getDetachmentsForFaction(factionId: String): List<Detachment>
    suspend fun getDetachment(id: String): Detachment?
    suspend fun getUnitsForFaction(factionId: String): List<Unit>
    suspend fun getUnit(id: String): Unit?
    suspend fun getCatalogueSnapshot(factionId: String, detachmentId: String): CatalogueSnapshot?
}
