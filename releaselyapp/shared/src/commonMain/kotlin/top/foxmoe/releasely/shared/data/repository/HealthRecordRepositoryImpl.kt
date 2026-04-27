package top.foxmoe.releasely.shared.data.repository

import kotlinx.datetime.Clock
import top.foxmoe.releasely.shared.data.local.LocalDatabase
import top.foxmoe.releasely.shared.data.local.epochSecToDayLabel
import top.foxmoe.releasely.shared.domain.model.DailyTrend
import top.foxmoe.releasely.shared.domain.model.HealthRecord
import top.foxmoe.releasely.shared.domain.model.RecordType
import top.foxmoe.releasely.shared.domain.repository.HealthRecordRepository

class HealthRecordRepositoryImpl(
    private val localDatabase: LocalDatabase
) : HealthRecordRepository {

    override suspend fun save(record: HealthRecord): Result<Unit> {
        return runCatching {
            localDatabase.insertRecord(
                id = record.id,
                type = record.type.name,
                recordEpochSec = record.timestampMillis / 1000,
                protectionEnabled = record.protectionEnabled,
                pleasureLevel = record.pleasureLevel.toLong()
            )
        }
    }

    override suspend fun getRecent(limit: Long): List<HealthRecord> {
        return localDatabase.getRecentRecords(limit).map {
            HealthRecord(
                id = it.id,
                type = RecordType.valueOf(it.record_type),
                timestampMillis = it.record_time * 1000,
                protectionEnabled = it.protection_enabled == 1L,
                pleasureLevel = it.pleasure_level.toInt()
            )
        }
    }

    override suspend fun getWeeklyTrend(): List<DailyTrend> {
        val nowSec = Clock.System.now().epochSeconds
        val sevenDaysAgo = nowSec - (6 * 24 * 60 * 60)

        val grouped = localDatabase.getRecordsBetween(sevenDaysAgo, nowSec)
            .groupBy { epochSecToDayLabel(it.record_time) }

        return grouped.entries.map { (day, records) ->
            DailyTrend(
                dayLabel = day,
                score = (records.sumOf { it.pleasure_level } / records.size).toInt()
            )
        }
    }

    override suspend fun getCurrentStreakDays(): Int {
        val recent = getRecent(limit = 90)
        if (recent.isEmpty()) return 0

        val uniqueDays = recent
            .map { it.timestampMillis / (24 * 60 * 60 * 1000L) }
            .toSet()
            .sortedDescending()

        var streak = 0
        var expectedDay = Clock.System.now().toEpochMilliseconds() / (24 * 60 * 60 * 1000L)
        uniqueDays.forEach { day ->
            if (day == expectedDay || day == expectedDay - 1 && streak == 0) {
                streak += 1
                expectedDay = day - 1
            } else if (day == expectedDay) {
                streak += 1
                expectedDay -= 1
            } else {
                return streak
            }
        }
        return streak
    }

    override suspend fun clearAllRecords(): Result<Unit> {
        return runCatching {
            localDatabase.clearAllRecords()
        }
    }
}
