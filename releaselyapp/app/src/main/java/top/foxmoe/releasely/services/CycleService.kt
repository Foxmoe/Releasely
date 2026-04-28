package top.foxmoe.releasely.services

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

data class CycleRecord(
    val id: String,
    val startDate: Long,
    val duration: Int?,
    val predictedNext: Long?,
    val isDeleted: Boolean,
    val createdAt: Long
)

class CycleService(private val queries: top.foxmoe.releasely.database.AppDatabaseQueries) {

    suspend fun getAllCycles(): List<CycleRecord> = withContext(Dispatchers.IO) {
        queries.getAllCycles().executeAsList().map { row ->
            CycleRecord(
                id = row.id,
                startDate = row.startDate,
                duration = row.duration?.toInt(),
                predictedNext = row.predictedNext,
                isDeleted = row.isDeleted == 1L,
                createdAt = row.createdAt
            )
        }
    }

    suspend fun getCycleById(id: String): CycleRecord? = withContext(Dispatchers.IO) {
        queries.getCycleById(id).executeAsOneOrNull()?.let { row ->
            CycleRecord(
                id = row.id,
                startDate = row.startDate,
                duration = row.duration?.toInt(),
                predictedNext = row.predictedNext,
                isDeleted = row.isDeleted == 1L,
                createdAt = row.createdAt
            )
        }
    }

    suspend fun insertCycle(startDate: Long, duration: Int?, predictedNext: Long?): String =
        withContext(Dispatchers.IO) {
            val id = UUID.randomUUID().toString()
            queries.insertCycle(
                id = id,
                startDate = startDate,
                duration = duration?.toLong(),
                predictedNext = predictedNext
            )
            id
        }

    suspend fun updateCycle(id: String, startDate: Long, duration: Int?, predictedNext: Long?) =
        withContext(Dispatchers.IO) {
            queries.updateCycle(
                startDate = startDate,
                duration = duration?.toLong(),
                predictedNext = predictedNext,
                id = id
            )
        }

    suspend fun deleteCycle(id: String) = withContext(Dispatchers.IO) {
        queries.deleteCycle(id)
    }

    suspend fun getLatestCycle(): CycleRecord? = withContext(Dispatchers.IO) {
        queries.getLatestCycle().executeAsOneOrNull()?.let { row ->
            CycleRecord(
                id = row.id,
                startDate = row.startDate,
                duration = row.duration?.toInt(),
                predictedNext = row.predictedNext,
                isDeleted = row.isDeleted == 1L,
                createdAt = row.createdAt
            )
        }
    }

    suspend fun getCycleCount(): Long = withContext(Dispatchers.IO) {
        queries.countCycles().executeAsOne()
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