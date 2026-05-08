package com.rosterforge.wh40k.di

import com.rosterforge.wh40k.data.repository.AppSettingsRepositoryImpl
import com.rosterforge.wh40k.data.repository.CatalogueRepositoryImpl
import com.rosterforge.wh40k.data.repository.DataSyncRepositoryImpl
import com.rosterforge.wh40k.data.repository.RosterRepositoryImpl
import com.rosterforge.wh40k.data.repository.StratagemRepositoryImpl
import com.rosterforge.wh40k.domain.repository.AppSettingsRepository
import com.rosterforge.wh40k.domain.repository.CatalogueRepository
import com.rosterforge.wh40k.domain.repository.DataSyncRepository
import com.rosterforge.wh40k.domain.repository.RosterRepository
import com.rosterforge.wh40k.domain.repository.StratagemRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindRosterRepository(impl: RosterRepositoryImpl): RosterRepository

    @Binds
    @Singleton
    abstract fun bindCatalogueRepository(impl: CatalogueRepositoryImpl): CatalogueRepository

    @Binds
    @Singleton
    abstract fun bindStratagemRepository(impl: StratagemRepositoryImpl): StratagemRepository

    @Binds
    @Singleton
    abstract fun bindAppSettingsRepository(impl: AppSettingsRepositoryImpl): AppSettingsRepository

    @Binds
    @Singleton
    abstract fun bindDataSyncRepository(impl: DataSyncRepositoryImpl): DataSyncRepository
}
