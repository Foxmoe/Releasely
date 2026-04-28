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

class CycleService(private val database: top.foxmoe.releasely.database.AppDatabase) {

    suspend fun getAllCycles(): List<CycleRecord> = withContext(Dispatchers.IO) {
        database.cycleQueries.getAllCycles().executeAsList().map { row ->
            CycleRecord(
                id = row.id,
                startDate = row.start_date,
                duration = row.duration?.toInt(),
                predictedNext = row.predicted_next,
                isDeleted = row.is_deleted == 1L,
                createdAt = row.created_at
            )
        }
    }

    suspend fun getCycleById(id: String): CycleRecord? = withContext(Dispatchers.IO) {
        database.cycleQueries.getCycleById(id).executeAsOneOrNull()?.let { row ->
            CycleRecord(
                id = row.id,
                startDate = row.start_date,
                duration = row.duration?.toInt(),
                predictedNext = row.predicted_next,
                isDeleted = row.is_deleted == 1L,
                createdAt = row.created_at
            )
        }
    }

    suspend fun insertCycle(startDate: Long, duration: Int?, predictedNext: Long?): String =
        withContext(Dispatchers.IO) {
            val id = UUID.randomUUID().toString()
            database.cycleQueries.insertCycle(
                id = id,
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
        database.cycleQueries.deleteCycle(id)
    }

    suspend fun getLatestCycle(): CycleRecord? = withContext(Dispatchers.IO) {
        database.cycleQueries.getLatestCycle().executeAsOneOrNull()?.let { row ->
            CycleRecord(
                id = row.id,
                startDate = row.start_date,
                duration = row.duration?.toInt(),
                predictedNext = row.predicted_next,
                isDeleted = row.is_deleted == 1L,
                createdAt = row.created_at
            )
        }
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