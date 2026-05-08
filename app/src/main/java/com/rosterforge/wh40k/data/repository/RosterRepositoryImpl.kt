package com.rosterforge.wh40k.data.repository

import androidx.room.withTransaction
import com.rosterforge.wh40k.data.local.AppDatabase
import com.rosterforge.wh40k.data.local.dao.RosterDao
import com.rosterforge.wh40k.data.local.dao.RosterUnitDao
import com.rosterforge.wh40k.data.mapper.toDomain
import com.rosterforge.wh40k.data.mapper.toEntity
import com.rosterforge.wh40k.domain.model.Roster
import com.rosterforge.wh40k.domain.model.RosterUnit
import com.rosterforge.wh40k.domain.repository.RosterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RosterRepositoryImpl @Inject constructor(
    private val database: AppDatabase,
    private val rosterDao: RosterDao,
    private val rosterUnitDao: RosterUnitDao,
) : RosterRepository {

    override fun observeRosters(): Flow<List<Roster>> =
        rosterDao.observeAll().flatMapLatest { rosterEntities ->
            if (rosterEntities.isEmpty()) {
                flowOf(emptyList())
            } else {
                combine(
                    rosterEntities.map { roster ->
                        rosterUnitDao.observeForRoster(roster.id).map { units ->
                            roster.toDomain(units.map { it.toDomain() })
                        }
                    },
                ) { it.toList() }
            }
        }

    override fun observeRoster(id: String): Flow<Roster?> =
        rosterDao.observeById(id).flatMapLatest { entity ->
            if (entity == null) flowOf(null)
            else rosterUnitDao.observeForRoster(id).map { units ->
                entity.toDomain(units.map { it.toDomain() })
            }
        }

    override suspend fun getRoster(id: String): Roster? {
        val entity = rosterDao.getById(id) ?: return null
        val units = rosterUnitDao.getForRoster(id).map { it.toDomain() }
        return entity.toDomain(units)
    }

    override suspend fun upsertRoster(roster: Roster) {
        database.withTransaction {
            rosterDao.upsert(roster.toEntity())
            // Replace unit set for this roster with the supplied list.
            rosterUnitDao.deleteAllForRoster(roster.id)
            rosterUnitDao.upsertAll(roster.units.map { it.toEntity() })
        }
    }

    override suspend fun deleteRoster(id: String) = rosterDao.deleteById(id)

    override suspend fun renameRoster(id: String, newName: String) {
        rosterDao.rename(id, newName, System.currentTimeMillis())
    }

    override suspend fun duplicateRoster(sourceId: String, newName: String): String {
        val source = getRoster(sourceId) ?: error("Roster $sourceId not found")
        val now = System.currentTimeMillis()
        val newId = UUID.randomUUID().toString()
        // Re-key roster_units so they reference the new roster but keep wargear/leader links.
        val idMap = source.units.associate { it.id to UUID.randomUUID().toString() }
        val rekeyedUnits = source.units.map { unit ->
            unit.copy(
                id = idMap.getValue(unit.id),
                rosterId = newId,
                attachedLeaderRosterUnitId = unit.attachedLeaderRosterUnitId?.let { idMap[it] },
            )
        }
        val duplicate = source.copy(
            id = newId,
            name = newName,
            units = rekeyedUnits,
            createdAt = now,
            updatedAt = now,
        )
        upsertRoster(duplicate)
        return newId
    }

    override suspend fun upsertRosterUnit(unit: RosterUnit) {
        database.withTransaction {
            rosterUnitDao.upsert(unit.toEntity())
            // Bump roster updatedAt so list ordering reflects the change.
            rosterDao.getById(unit.rosterId)?.let { roster ->
                rosterDao.upsert(roster.copy(updatedAt = System.currentTimeMillis()))
            }
        }
    }

    override suspend fun deleteRosterUnit(rosterUnitId: String) {
        rosterUnitDao.deleteById(rosterUnitId)
    }
}
