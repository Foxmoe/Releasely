package top.foxmoe.releasely.service

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import org.springframework.stereotype.Service
import top.foxmoe.releasely.entity.ActivityRecord
import top.foxmoe.releasely.entity.HealthReport
import top.foxmoe.releasely.mapper.ActivityRecordMapper
import top.foxmoe.releasely.mapper.HealthReportMapper
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class HealthReportService(
    private val healthReportMapper: HealthReportMapper,
    private val activityRecordMapper: ActivityRecordMapper
) {

    fun getReportsByUserId(userId: Long): List<HealthReport> {
        val wrapper = QueryWrapper<HealthReport>()
            .eq("user_id", userId)
            .orderByDesc("created_at")
        return healthReportMapper.selectList(wrapper)
    }

    fun getReportById(id: Long): HealthReport? {
        return healthReportMapper.selectById(id)
    }

    fun getReportByPeriod(userId: Long, period: String): HealthReport? {
        val wrapper = QueryWrapper<HealthReport>()
            .eq("user_id", userId)
            .eq("period", period)
        return healthReportMapper.selectOne(wrapper)
    }

    fun createReport(report: HealthReport): Long {
        report.createdAt = LocalDateTime.now()
        healthReportMapper.insert(report)
        return report.id ?: 0L
    }

    fun updateReport(report: HealthReport): Boolean {
        return healthReportMapper.updateById(report) > 0
    }

    fun deleteReport(id: Long): Boolean {
        return healthReportMapper.deleteById(id) > 0
    }

    fun generateReport(userId: Long, days: Long, periodType: String): HealthReport {
        val endDate = LocalDateTime.now()
        val startDate = endDate.minusDays(days)

        val wrapper = QueryWrapper<ActivityRecord>()
            .eq("user_id", userId)
            .ge("occurred_at", startDate)
            .le("occurred_at", endDate)

        val records = activityRecordMapper.selectList(wrapper)
        val frequencyData = buildFrequencyData(records)

        val report = HealthReport(
            userId = userId,
            period = "${periodType}_${endDate.format(DateTimeFormatter.ISO_DATE)}",
            frequencyData = frequencyData.first,
            protectionRate = frequencyData.second
        )

        createReport(report)
        return report
    }

    private fun buildFrequencyData(records: List<ActivityRecord>): Pair<String, Double> {
        val totalActivities = records.size
        val protectedActivities = records.count { r ->
            val p = r.protection
            p != null && p.isNotEmpty()
        }
        val protectionRate = if (totalActivities > 0) {
            (protectedActivities.toDouble() / totalActivities) * 100
        } else {
            0.0
        }

        val frequencyData = """
            {
                "total": $totalActivities,
                "protected": $protectedActivities,
                "unprotected": ${totalActivities - protectedActivities},
                "averagePleasure": ${records.mapNotNull { it.pleasureRating }.average().takeIf { !it.isNaN() } ?: 0}
            }
        """.trimIndent()

        return Pair(frequencyData, protectionRate)
    }

    fun generateWeeklyReport(userId: Long): HealthReport {
        return generateReport(userId, 7, "weekly")
    }

    fun generateMonthlyReport(userId: Long): HealthReport {
        return generateReport(userId, 30, "monthly")
    }
}