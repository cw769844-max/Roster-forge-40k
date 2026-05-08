package com.rosterforge.wh40k.domain.repository

import com.rosterforge.wh40k.domain.model.Roster
import com.rosterforge.wh40k.domain.model.RosterUnit
import kotlinx.coroutines.flow.Flow

interface RosterRepository {
    fun observeRosters(): Flow<List<Roster>>
    fun observeRoster(id: String): Flow<Roster?>
    suspend fun getRoster(id: String): Roster?
    suspend fun upsertRoster(roster: Roster)
    suspend fun deleteRoster(id: String)
    suspend fun renameRoster(id: String, newName: String)
    suspend fun duplicateRoster(sourceId: String, newName: String): String

    suspend fun upsertRosterUnit(unit: RosterUnit)
    suspend fun deleteRosterUnit(rosterUnitId: String)
}
