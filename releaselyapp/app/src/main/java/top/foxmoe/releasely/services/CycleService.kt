package top.foxmoe.releasely.services

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

data class CycleRecord(
    val id: String,
    val serverId: String?,
    val startDate: Long,
    val duration: Int?,
    val predictedNext: Long?,
    val isDeleted: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)

class CycleService(private val database: top.foxmoe.releasely.database.AppDatabase) {

    suspend fun getAllCycles(): List<CycleRecord> = withContext(Dispatchers.IO) {
        database.cycleQueries.getAllCycles().executeAsList().map { row ->
            CycleRecord(
                id = row.id,
                serverId = row.server_id,
                startDate = row.start_date,
                duration = row.duration?.toInt(),
                predictedNext = row.predicted_next,
                isDeleted = row.is_deleted == 1L,
                createdAt = row.created_at,
                updatedAt = row.updated_at
            )
        }
    }

    suspend fun getCycleById(id: String): CycleRecord? = withContext(Dispatchers.IO) {
        database.cycleQueries.getCycleById(id).executeAsOneOrNull()?.let { row ->
            CycleRecord(
                id = row.id,
                serverId = row.server_id,
                startDate = row.start_date,
                duration = row.duration?.toInt(),
                predictedNext = row.predicted_next,
                isDeleted = row.is_deleted == 1L,
                createdAt = row.created_at,
                updatedAt = row.updated_at
            )
        }
    }

    suspend fun getCycleByServerId(serverId: String): CycleRecord? = withContext(Dispatchers.IO) {
        database.cycleQueries.getCycleByServerId(serverId).executeAsOneOrNull()?.let { row ->
            CycleRecord(
                id = row.id,
                serverId = row.server_id,
                startDate = row.start_date,
                duration = row.duration?.toInt(),
                predictedNext = row.predicted_next,
                isDeleted = row.is_deleted == 1L,
                createdAt = row.created_at,
                updatedAt = row.updated_at
            )
        }
    }

    suspend fun insertCycle(startDate: Long, duration: Int?, predictedNext: Long?): String =
        withContext(Dispatchers.IO) {
            val id = UUID.randomUUID().toString()
            database.cycleQueries.insertCycle(
                id = id,
                server_id = null,
                start_date = startDate,
                duration = duration?.toLong(),
                predicted_next = predictedNext
            )
            id
        }

    suspend fun updateCycle(id: String, startDate: Long, duration: Int?, predictedNext: Long?) =
        withContext(Dispatchers.IO) {
            database.cycleQueries.updateCycle(
                start_date = startDate,
                duration = duration?.toLong(),
                predicted_next = predictedNext,
                id = id
            )
        }

    suspend fun deleteCycle(id: String) = withContext(Dispatchers.IO) {
        database.cycleQueries.markCycleDeleted(id)
    }

    suspend fun getLatestCycle(): CycleRecord? = withContext(Dispatchers.IO) {
        database.cycleQueries.getLatestCycle().executeAsOneOrNull()?.let { row ->
            CycleRecord(
                id = row.id,
                serverId = row.server_id,
                startDate = row.start_date,
                duration = row.duration?.toInt(),
                predictedNext = row.predicted_next,
                isDeleted = row.is_deleted == 1L,
                createdAt = row.created_at,
                updatedAt = row.updated_at
            )
        }
    }

    suspend fun getCyclesUpdatedSince(since: Long): List<CycleRecord> = withContext(Dispatchers.IO) {
        database.cycleQueries.getCyclesUpdatedSince(since).executeAsList().map { row ->
            CycleRecord(
                id = row.id,
                serverId = row.server_id,
                startDate = row.start_date,
                duration = row.duration?.toInt(),
                predictedNext = row.predicted_next,
                isDeleted = row.is_deleted == 1L,
                createdAt = row.created_at,
                updatedAt = row.updated_at
            )
        }
    }

    suspend fun setServerId(localId: String, serverId: String) = withContext(Dispatchers.IO) {
        database.cycleQueries.setServerId(serverId, localId)
    }

    suspend fun getCycleCount(): Long = withContext(Dispatchers.IO) {
        database.cycleQueries.countCycles().executeAsOne()
    }

    suspend fun predictNextPeriod(): Long? = withContext(Dispatchers.IO) {
        val cycles = getAllCycles()
        if (cycles.size < 2) return@withContext null

        val sorted = cycles.sortedByDescending { it.startDate }
        val latest = sorted.firstOrNull() ?: return@withContext null
        val previous = sorted.getOrNull(1) ?: return@withContext null

        val avgLength = ((latest.startDate - previous.startDate) / (24 * 60 * 60)).toInt()
        val nextPredicted = latest.startDate + (avgLength * 24 * 60 * 60)

        val latestId = latest.id
        updateCycle(latestId, latest.startDate, latest.duration, nextPredicted)

        nextPredicted
    }
}
