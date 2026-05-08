package com.rosterforge.wh40k.data.repository

import androidx.room.withTransaction
import com.rosterforge.wh40k.data.local.AppDatabase
import com.rosterforge.wh40k.data.local.dao.DataMetadataDao
import com.rosterforge.wh40k.data.local.dao.DetachmentDao
import com.rosterforge.wh40k.data.local.dao.EnhancementDao
import com.rosterforge.wh40k.data.local.dao.FactionDao
import com.rosterforge.wh40k.data.local.dao.StratagemDao
import com.rosterforge.wh40k.data.local.dao.UnitDao
import com.rosterforge.wh40k.data.mapper.toEntity
import com.rosterforge.wh40k.data.parser.BsXmlParser
import com.rosterforge.wh40k.data.remote.GithubApiService
import com.rosterforge.wh40k.domain.repository.AppSettingsRepository
import com.rosterforge.wh40k.domain.repository.DataSyncProgress
import com.rosterforge.wh40k.domain.repository.DataSyncRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataSyncRepositoryImpl @Inject constructor(
    private val database: AppDatabase,
    private val factionDao: FactionDao,
    private val detachmentDao: DetachmentDao,
    private val unitDao: UnitDao,
    private val enhancementDao: EnhancementDao,
    private val stratagemDao: StratagemDao,
    private val dataMetadataDao: DataMetadataDao,
    private val github: GithubApiService,
    private val httpClient: OkHttpClient,
    private val parser: BsXmlParser,
    private val settings: AppSettingsRepository,
) : DataSyncRepository {

    override suspend fun fetchLatestReleaseTag(): Result<String> = runCatching {
        github.getLatestRelease(owner = OWNER, repo = REPO).tagName
    }

    override fun syncCatalogue(): Flow<DataSyncProgress> = flow {
        emit(DataSyncProgress.Started)
        val release = runCatching {
            github.getLatestRelease(owner = OWNER, repo = REPO)
        }.getOrElse {
            emit(DataSyncProgress.Failed(it)); return@flow
        }
        val asset = release.assets.firstOrNull { it.name.endsWith(".zip") }
            ?: run {
                emit(DataSyncProgress.Failed(IllegalStateException("No ZIP asset in release")))
                return@flow
            }
        emit(DataSyncProgress.Downloading(0))
        val response = runCatching {
            httpClient.newCall(Request.Builder().url(asset.downloadUrl).build()).execute()
        }.getOrElse { emit(DataSyncProgress.Failed(it)); return@flow }
        val body = response.body
            ?: run { emit(DataSyncProgress.Failed(IllegalStateException("Empty body"))); return@flow }

        emit(DataSyncProgress.Parsing)
        val result = runCatching { parser.parseRelease(body.byteStream()) }
            .getOrElse { emit(DataSyncProgress.Failed(it)); return@flow }
        body.close()

        emit(DataSyncProgress.Saving)
        runCatching {
            database.withTransaction {
                factionDao.deleteAll()                // cascades remove children
                factionDao.insertAll(result.factions.map { it.toEntity() })
                detachmentDao.insertAll(result.detachments.map { it.toEntity() })
                unitDao.insertAll(result.units.map { it.toEntity() })
                enhancementDao.insertAll(result.enhancements.map { it.toEntity() })
                stratagemDao.insertAll(result.stratagems.map { it.toEntity() })
            }
            settings.setInstalledDataVersion(release.tagName)
            settings.setInstalledDataDate(System.currentTimeMillis())
        }.onFailure { emit(DataSyncProgress.Failed(it)); return@flow }

        emit(DataSyncProgress.Done(release.tagName))
    }

    private companion object {
        const val OWNER = "BSData"
        const val REPO = "wh40k-10e"
    }
}
