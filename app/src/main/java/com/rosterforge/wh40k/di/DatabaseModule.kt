package com.rosterforge.wh40k.di

import android.content.Context
import androidx.room.Room
import com.rosterforge.wh40k.data.local.AppDatabase
import com.rosterforge.wh40k.data.local.dao.DataMetadataDao
import com.rosterforge.wh40k.data.local.dao.DetachmentDao
import com.rosterforge.wh40k.data.local.dao.EnhancementDao
import com.rosterforge.wh40k.data.local.dao.FactionDao
import com.rosterforge.wh40k.data.local.dao.RosterDao
import com.rosterforge.wh40k.data.local.dao.RosterUnitDao
import com.rosterforge.wh40k.data.local.dao.StratagemDao
import com.rosterforge.wh40k.data.local.dao.UnitDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.NAME)
            .fallbackToDestructiveMigration()      // dev only; replace before 1.0
            .build()

    @Provides fun provideFactionDao(db: AppDatabase): FactionDao = db.factionDao()
    @Provides fun provideDetachmentDao(db: AppDatabase): DetachmentDao = db.detachmentDao()
    @Provides fun provideUnitDao(db: AppDatabase): UnitDao = db.unitDao()
    @Provides fun provideEnhancementDao(db: AppDatabase): EnhancementDao = db.enhancementDao()
    @Provides fun provideStratagemDao(db: AppDatabase): StratagemDao = db.stratagemDao()
    @Provides fun provideDataMetadataDao(db: AppDatabase): DataMetadataDao = db.dataMetadataDao()
    @Provides fun provideRosterDao(db: AppDatabase): RosterDao = db.rosterDao()
    @Provides fun provideRosterUnitDao(db: AppDatabase): RosterUnitDao = db.rosterUnitDao()
}
