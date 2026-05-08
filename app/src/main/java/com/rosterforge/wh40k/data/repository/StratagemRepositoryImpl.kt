package com.rosterforge.wh40k.data.repository

import com.rosterforge.wh40k.data.local.dao.StratagemDao
import com.rosterforge.wh40k.data.mapper.toDomain
import com.rosterforge.wh40k.domain.model.Stratagem
import com.rosterforge.wh40k.domain.repository.StratagemRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StratagemRepositoryImpl @Inject constructor(
    private val stratagemDao: StratagemDao,
) : StratagemRepository {

    override fun observeStratagemsForArmy(
        factionId: String,
        detachmentId: String,
    ): Flow<List<Stratagem>> =
        stratagemDao.observeForArmy(factionId, detachmentId).map { list ->
            list.map { it.toDomain() }
        }

    override suspend fun getCoreStratagems(): List<Stratagem> =
        stratagemDao.getCore().map { it.toDomain() }
}
