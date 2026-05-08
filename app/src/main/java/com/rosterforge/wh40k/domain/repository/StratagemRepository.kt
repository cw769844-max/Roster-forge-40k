package com.rosterforge.wh40k.domain.repository

import com.rosterforge.wh40k.domain.model.Stratagem
import kotlinx.coroutines.flow.Flow

interface StratagemRepository {
    fun observeStratagemsForArmy(factionId: String, detachmentId: String): Flow<List<Stratagem>>
    suspend fun getCoreStratagems(): List<Stratagem>
}
