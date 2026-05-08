package com.rosterforge.wh40k.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rosterforge.wh40k.data.local.entity.DataMetadataEntity
import com.rosterforge.wh40k.data.local.entity.DetachmentEntity
import com.rosterforge.wh40k.data.local.entity.EnhancementEntity
import com.rosterforge.wh40k.data.local.entity.FactionEntity
import com.rosterforge.wh40k.data.local.entity.StratagemEntity
import com.rosterforge.wh40k.data.local.entity.UnitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FactionDao {
    @Query("SELECT * FROM factions ORDER BY name ASC")
    suspend fun getAll(): List<FactionEntity>

    @Query("SELECT * FROM factions WHERE id = :id")
    suspend fun getById(id: String): FactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(factions: List<FactionEntity>)

    @Query("SELECT COUNT(*) FROM factions")
    suspend fun countAll(): Int

    @Query("DELETE FROM factions")
    suspend fun deleteAll()
}

@Dao
interface DetachmentDao {
    @Query("SELECT * FROM detachments WHERE factionId = :factionId ORDER BY name ASC")
    suspend fun getForFaction(factionId: String): List<DetachmentEntity>

    @Query("SELECT * FROM detachments WHERE id = :id")
    suspend fun getById(id: String): DetachmentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(detachments: List<DetachmentEntity>)
}

@Dao
interface UnitDao {
    @Query("SELECT * FROM units WHERE factionId = :factionId ORDER BY name ASC")
    suspend fun getForFaction(factionId: String): List<UnitEntity>

    @Query("SELECT * FROM units WHERE id = :id")
    suspend fun getById(id: String): UnitEntity?

    @Query("SELECT * FROM units WHERE id IN (:ids)")
    suspend fun getByIds(ids: List<String>): List<UnitEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(units: List<UnitEntity>)
}

@Dao
interface EnhancementDao {
    @Query("SELECT * FROM enhancements WHERE detachmentId = :detachmentId ORDER BY name ASC")
    suspend fun getForDetachment(detachmentId: String): List<EnhancementEntity>

    @Query("SELECT * FROM enhancements WHERE id = :id")
    suspend fun getById(id: String): EnhancementEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(enhancements: List<EnhancementEntity>)
}

@Dao
interface StratagemDao {
    @Query(
        """
        SELECT * FROM stratagems
        WHERE factionId IS NULL
           OR factionId = :factionId
           OR detachmentId = :detachmentId
        ORDER BY cp ASC, name ASC
        """,
    )
    fun observeForArmy(factionId: String, detachmentId: String): Flow<List<StratagemEntity>>

    @Query("SELECT * FROM stratagems WHERE factionId IS NULL ORDER BY cp ASC, name ASC")
    suspend fun getCore(): List<StratagemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(stratagems: List<StratagemEntity>)
}

@Dao
interface DataMetadataDao {
    @Query("SELECT value FROM data_metadata WHERE key = :key")
    suspend fun get(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun put(entry: DataMetadataEntity)
}
