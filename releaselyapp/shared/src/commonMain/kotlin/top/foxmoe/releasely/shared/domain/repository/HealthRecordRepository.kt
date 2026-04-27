package top.foxmoe.releasely.shared.domain.repository

import top.foxmoe.releasely.shared.domain.model.DailyTrend
import top.foxmoe.releasely.shared.domain.model.HealthRecord

interface HealthRecordRepository {
    suspend fun save(record: HealthRecord): Result<Unit>
    suspend fun getRecent(limit: Long = 20): List<HealthRecord>
    suspend fun getWeeklyTrend(): List<DailyTrend>
    suspend fun getCurrentStreakDays(): Int
    suspend fun clearAllRecords(): Result<Unit>
}
