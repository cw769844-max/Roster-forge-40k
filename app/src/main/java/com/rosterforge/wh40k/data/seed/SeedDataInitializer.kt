package com.rosterforge.wh40k.data.seed

import android.content.Context
import android.util.Log
import androidx.room.withTransaction
import com.rosterforge.wh40k.data.local.AppDatabase
import com.rosterforge.wh40k.data.local.dao.DetachmentDao
import com.rosterforge.wh40k.data.local.dao.EnhancementDao
import com.rosterforge.wh40k.data.local.dao.FactionDao
import com.rosterforge.wh40k.data.local.dao.StratagemDao
import com.rosterforge.wh40k.data.local.dao.UnitDao
import com.rosterforge.wh40k.data.mapper.toEntity
import com.rosterforge.wh40k.data.parser.BsParserResult
import com.rosterforge.wh40k.data.parser.BsXmlParser
import com.rosterforge.wh40k.domain.repository.AppSettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SeedDataInitializer @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: AppDatabase,
    private val factionDao: FactionDao,
    private val detachmentDao: DetachmentDao,
    private val unitDao: UnitDao,
    private val enhancementDao: EnhancementDao,
    private val stratagemDao: StratagemDao,
    private val parser: BsXmlParser,
    private val settings: AppSettingsRepository,
) {

    suspend fun seedIfEmpty() = withContext(Dispatchers.IO) {
        if (factionDao.countAll() > 0) return@withContext
        loadAndPersist()
    }

    suspend fun resetToSeedData() = withContext(Dispatchers.IO) {
        loadAndPersist()
    }

    private suspend fun loadAndPersist() {
        val (parsed, version) = loadFromBundledAsset()
            ?: parsedFromSampleSeed()
        database.withTransaction {
            factionDao.deleteAll()
            factionDao.insertAll(parsed.factions.map { it.toEntity() })
            detachmentDao.insertAll(parsed.detachments.map { it.toEntity() })
            unitDao.insertAll(parsed.units.map { it.toEntity() })
            enhancementDao.insertAll(parsed.enhancements.map { it.toEntity() })
            stratagemDao.insertAll(parsed.stratagems.map { it.toEntity() })
        }
        settings.setInstalledDataVersion(version)
        settings.setInstalledDataDate(System.currentTimeMillis())
    }

    private fun loadFromBundledAsset(): Pair<BsParserResult, String>? {
        return runCatching {
            context.assets.open(BUNDLED_ASSET_NAME).use { stream ->
                parser.parseRelease(stream)
            }
        }
            .onFailure { Log.w(TAG, "Bundled BSData asset failed to parse; falling back to sample seed", it) }
            .getOrNull()
            ?.takeIf { it.factions.isNotEmpty() }
            ?.let { it to "wh40k-10e-bundled" }
    }

    private fun parsedFromSampleSeed(): Pair<BsParserResult, String> {
        Log.i(TAG, "Seeding hand-crafted SampleCatalogueSeed (no bundled asset)")
        val seed = SampleCatalogueSeed
        return BsParserResult(
            factions = seed.factions,
            detachments = seed.allDetachments,
            units = seed.allUnits,
            enhancements = seed.allEnhancements,
            stratagems = seed.allStratagems,
            warnings = emptyList(),
        ) to "sample-data"
    }

    private companion object {
        const val BUNDLED_ASSET_NAME = "wh40k-10e.zip"
        const val TAG = "SeedDataInitializer"
    }
}
