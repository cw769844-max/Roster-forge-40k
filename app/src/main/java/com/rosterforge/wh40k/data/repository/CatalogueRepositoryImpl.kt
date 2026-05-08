package com.rosterforge.wh40k.data.repository

import com.rosterforge.wh40k.data.local.dao.DetachmentDao
import com.rosterforge.wh40k.data.local.dao.EnhancementDao
import com.rosterforge.wh40k.data.local.dao.FactionDao
import com.rosterforge.wh40k.data.local.dao.StratagemDao
import com.rosterforge.wh40k.data.local.dao.UnitDao
import com.rosterforge.wh40k.data.mapper.toDomain
import com.rosterforge.wh40k.domain.model.CatalogueSnapshot
import com.rosterforge.wh40k.domain.model.Detachment
import com.rosterforge.wh40k.domain.model.Faction
import com.rosterforge.wh40k.domain.model.Unit
import com.rosterforge.wh40k.domain.repository.CatalogueRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CatalogueRepositoryImpl @Inject constructor(
    private val factionDao: FactionDao,
    private val detachmentDao: DetachmentDao,
    private val unitDao: UnitDao,
    private val enhancementDao: EnhancementDao,
    private val stratagemDao: StratagemDao,
) : CatalogueRepository {

    override suspend fun getAllFactions(): List<Faction> =
        factionDao.getAll().map { it.toDomain() }

    override suspend fun getFaction(id: String): Faction? =
        factionDao.getById(id)?.toDomain()

    override suspend fun getDetachmentsForFaction(factionId: String): List<Detachment> =
        detachmentDao.getForFaction(factionId).map { entity ->
            entity.toDomain(
                enhancements = enhancementDao.getForDetachment(entity.id).map { it.toDomain() },
                stratagems = emptyList(),     // populated only when full Detachment requested
            )
        }

    override suspend fun getDetachment(id: String): Detachment? {
        val entity = detachmentDao.getById(id) ?: return null
        val enhancements = enhancementDao.getForDetachment(id).map { it.toDomain() }
        return entity.toDomain(enhancements = enhancements, stratagems = emptyList())
    }

    override suspend fun getUnitsForFaction(factionId: String): List<Unit> =
        unitDao.getForFaction(factionId).map { it.toDomain() }

    override suspend fun getUnit(id: String): Unit? = unitDao.getById(id)?.toDomain()

    override suspend fun getCatalogueSnapshot(
        factionId: String,
        detachmentId: String,
    ): CatalogueSnapshot? {
        val faction = factionDao.getById(factionId)?.toDomain() ?: return null
        val detachmentEntity = detachmentDao.getById(detachmentId) ?: return null
        val enhancements = enhancementDao.getForDetachment(detachmentId).map { it.toDomain() }
        val units = unitDao.getForFaction(factionId).map { it.toDomain() }
        val detachment = detachmentEntity.toDomain(
            enhancements = enhancements,
            stratagems = emptyList(),
        )
        return CatalogueSnapshot(
            faction = faction,
            detachment = detachment,
            unitsById = units.associateBy { it.id },
            enhancementsById = enhancements.associateBy { it.id },
        )
    }
}
