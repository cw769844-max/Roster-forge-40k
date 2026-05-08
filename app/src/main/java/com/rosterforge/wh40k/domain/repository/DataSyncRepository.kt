package com.rosterforge.wh40k.domain.repository

import kotlinx.coroutines.flow.Flow

interface DataSyncRepository {
    suspend fun fetchLatestReleaseTag(): Result<String>
    fun syncCatalogue(): Flow<DataSyncProgress>
}

sealed interface DataSyncProgress {
    object Started : DataSyncProgress
    data class Downloading(val percent: Int) : DataSyncProgress
    object Parsing : DataSyncProgress
    object Saving : DataSyncProgress
    data class Done(val newVersion: String) : DataSyncProgress
    data class Failed(val cause: Throwable) : DataSyncProgress
}
