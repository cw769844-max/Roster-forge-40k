package com.rosterforge.wh40k.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rosterforge.wh40k.data.local.entity.RosterEntity
import com.rosterforge.wh40k.data.local.entity.RosterUnitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RosterDao {
    @Query("SELECT * FROM rosters ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<RosterEntity>>

    @Query("SELECT * FROM rosters WHERE id = :id")
    fun observeById(id: String): Flow<RosterEntity?>

    @Query("SELECT * FROM rosters WHERE id = :id")
    suspend fun getById(id: String): RosterEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(roster: RosterEntity)

    @Query("DELETE FROM rosters WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("UPDATE rosters SET name = :newName, updatedAt = :timestamp WHERE id = :id")
    suspend fun rename(id: String, newName: String, timestamp: Long)
}

@Dao
interface RosterUnitDao {
    @Query("SELECT * FROM roster_units WHERE rosterId = :rosterId ORDER BY sortOrder ASC")
    fun observeForRoster(rosterId: String): Flow<List<RosterUnitEntity>>

    @Query("SELECT * FROM roster_units WHERE rosterId = :rosterId ORDER BY sortOrder ASC")
    suspend fun getForRoster(rosterId: String): List<RosterUnitEntity>

    @Query("SELECT * FROM roster_units WHERE id = :id")
    suspend fun getById(id: String): RosterUnitEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(unit: RosterUnitEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(units: List<RosterUnitEntity>)

    @Query("DELETE FROM roster_units WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM roster_units WHERE rosterId = :rosterId")
    suspend fun deleteAllForRoster(rosterId: String)
}
