package com.rosterforge.wh40k.data.seed

import androidx.room.withTransaction
import com.rosterforge.wh40k.data.local.AppDatabase
import com.rosterforge.wh40k.data.local.dao.DataMetadataDao
import com.rosterforge.wh40k.data.local.dao.DetachmentDao
import com.rosterforge.wh40k.data.local.dao.EnhancementDao
import com.rosterforge.wh40k.data.local.dao.FactionDao
import com.rosterforge.wh40k.data.local.dao.StratagemDao
import com.rosterforge.wh40k.data.local.dao.UnitDao
import com.rosterforge.wh40k.data.mapper.toEntity
import com.rosterforge.wh40k.domain.repository.AppSettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SeedDataInitializer @Inject constructor(
    private val database: AppDatabase,
    private val factionDao: FactionDao,
    private val detachmentDao: DetachmentDao,
    private val unitDao: UnitDao,
    private val enhancementDao: EnhancementDao,
    private val stratagemDao: StratagemDao,
    private val settings: AppSettingsRepository,
) {

    suspend fun seedIfEmpty() = withContext(Dispatchers.IO) {
        if (factionDao.countAll() > 0) return@withContext
        insertSeedData()
    }

    suspend fun resetToSeedData() = withContext(Dispatchers.IO) {
        insertSeedData()
    }

    private suspend fun insertSeedData() {
        val seed = SampleCatalogueSeed
        database.withTransaction {
            factionDao.deleteAll()
            factionDao.insertAll(seed.factions.map { it.toEntity() })
            detachmentDao.insertAll(seed.detachments.map { it.toEntity() })
            unitDao.insertAll(seed.units.map { it.toEntity() })
            enhancementDao.insertAll(seed.enhancements.map { it.toEntity() })
            stratagemDao.insertAll(seed.stratagems.map { it.toEntity() })
        }
        settings.setInstalledDataVersion("sample-data")
        settings.setInstalledDataDate(System.currentTimeMillis())
    }
}
