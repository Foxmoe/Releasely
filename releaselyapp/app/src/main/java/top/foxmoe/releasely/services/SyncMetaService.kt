package top.foxmoe.releasely.services

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SyncMetaService(private val database: top.foxmoe.releasely.database.AppDatabase) {

    suspend fun getLastSync(entityType: String): Long = withContext(Dispatchers.IO) {
        database.syncMetaQueries.getLastSync(entityType).executeAsOneOrNull() ?: 0L
    }

    suspend fun setLastSync(entityType: String, timestamp: Long) = withContext(Dispatchers.IO) {
        database.syncMetaQueries.setLastSync(entityType, timestamp)
    }
}
