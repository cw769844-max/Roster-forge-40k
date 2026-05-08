package com.rosterforge.wh40k.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.rosterforge.wh40k.data.local.dao.DataMetadataDao
import com.rosterforge.wh40k.data.local.dao.DetachmentDao
import com.rosterforge.wh40k.data.local.dao.EnhancementDao
import com.rosterforge.wh40k.data.local.dao.FactionDao
import com.rosterforge.wh40k.data.local.dao.RosterDao
import com.rosterforge.wh40k.data.local.dao.RosterUnitDao
import com.rosterforge.wh40k.data.local.dao.StratagemDao
import com.rosterforge.wh40k.data.local.dao.UnitDao
import com.rosterforge.wh40k.data.local.entity.DataMetadataEntity
import com.rosterforge.wh40k.data.local.entity.DetachmentEntity
import com.rosterforge.wh40k.data.local.entity.EnhancementEntity
import com.rosterforge.wh40k.data.local.entity.FactionEntity
import com.rosterforge.wh40k.data.local.entity.RosterEntity
import com.rosterforge.wh40k.data.local.entity.RosterUnitEntity
import com.rosterforge.wh40k.data.local.entity.StratagemEntity
import com.rosterforge.wh40k.data.local.entity.UnitEntity

@Database(
    version = 1,
    exportSchema = true,
    entities = [
        FactionEntity::class,
        DetachmentEntity::class,
        UnitEntity::class,
        EnhancementEntity::class,
        StratagemEntity::class,
        DataMetadataEntity::class,
        RosterEntity::class,
        RosterUnitEntity::class,
    ],
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun factionDao(): FactionDao
    abstract fun detachmentDao(): DetachmentDao
    abstract fun unitDao(): UnitDao
    abstract fun enhancementDao(): EnhancementDao
    abstract fun stratagemDao(): StratagemDao
    abstract fun dataMetadataDao(): DataMetadataDao
    abstract fun rosterDao(): RosterDao
    abstract fun rosterUnitDao(): RosterUnitDao

    companion object {
        const val NAME = "rosterforge.db"
    }
}
