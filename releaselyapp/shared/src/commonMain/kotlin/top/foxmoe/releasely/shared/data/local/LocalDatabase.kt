package top.foxmoe.releasely.shared.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import top.foxmoe.releasely.shared.db.ReleaselyDatabase
import top.foxmoe.releasely.shared.db.Record

class LocalDatabase(
    driverFactory: DatabaseDriverFactory
) {
    private val database = ReleaselyDatabase(driverFactory.createDriver())
    private val recordQueries = database.recordQueries

    fun observeRecentRecords(limit: Long): Flow<List<Record>> {
        return recordQueries.getRecentRecords(limit).asFlow().mapToList(Dispatchers.Default)
    }

    fun getRecentRecords(limit: Long): List<Record> {
        return recordQueries.getRecentRecords(limit).executeAsList()
    }

    fun getRecordsBetween(startEpochSec: Long, endEpochSec: Long): List<Record> {
        return recordQueries.getRecordsBetween(startEpochSec, endEpochSec).executeAsList()
    }

    fun insertRecord(
        id: String,
        type: String,
        recordEpochSec: Long,
        protectionEnabled: Boolean,
        pleasureLevel: Long
    ) {
        recordQueries.insertRecord(
            id = id,
            record_type = type,
            record_time = recordEpochSec,
            protection_enabled = if (protectionEnabled) 1 else 0,
            pleasure_level = pleasureLevel
        )
    }

    fun clearAllRecords() {
        recordQueries.clearAllRecords()
    }
}
