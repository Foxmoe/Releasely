package top.foxmoe.releasely.service

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import org.springframework.stereotype.Service
import top.foxmoe.releasely.entity.ActivityRecord
import top.foxmoe.releasely.entity.Cycle
import top.foxmoe.releasely.entity.Medication
import top.foxmoe.releasely.entity.Partner
import top.foxmoe.releasely.mapper.ActivityRecordMapper
import top.foxmoe.releasely.mapper.CycleMapper
import top.foxmoe.releasely.mapper.MedicationMapper
import top.foxmoe.releasely.mapper.PartnerMapper
import java.time.LocalDate

@Service
class DashboardService(
    private val activityMapper: ActivityRecordMapper,
    private val cycleMapper: CycleMapper,
    private val medicationMapper: MedicationMapper,
    private val partnerMapper: PartnerMapper
) {

    fun getStats(userId: Long): DashboardStats {
        val activityCount = activityMapper.selectCount(QueryWrapper<ActivityRecord>()
            .eq("user_id", userId).eq("is_deleted", false))

        val cycleCount = cycleMapper.selectCount(QueryWrapper<Cycle>()
            .eq("user_id", userId).eq("is_deleted", false))

        val activeMeds = medicationMapper.selectCount(QueryWrapper<Medication>()
            .eq("user_id", userId).eq("is_active", true))

        val partnerCount = partnerMapper.selectCount(QueryWrapper<Partner>()
            .eq("user_id", userId).eq("status", "ACTIVE"))

        return DashboardStats(
            totalActivities = activityCount.toInt(),
            totalCycles = cycleCount.toInt(),
            activeMedications = activeMeds.toInt(),
            partnerCount = partnerCount.toInt()
        )
    }

    fun getRecentActivities(userId: Long, limit: Int = 5): List<ActivityRecord> {
        return activityMapper.selectList(QueryWrapper<ActivityRecord>()
            .eq("user_id", userId)
            .eq("is_deleted", false)
            .orderByDesc("occurred_at")
            .last("LIMIT $limit"))
    }

    fun getCyclePrediction(userId: Long): CyclePrediction? {
        val cycles = cycleMapper.selectList(QueryWrapper<Cycle>()
            .eq("user_id", userId)
            .eq("is_deleted", false)
            .orderByDesc("start_date")
            .last("LIMIT 2"))

        if (cycles.size < 2) return null

        val sorted = cycles.sortedByDescending { it.startDate }
        val latest = sorted[0]
        val previous = sorted[1]

        val latestStart = latest.startDate ?: return null
        val previousStart = previous.startDate ?: return null

        val avgLength = java.time.temporal.ChronoUnit.DAYS.between(previousStart, latestStart).toInt()
        val predictedNext = latestStart.plusDays(avgLength.toLong())

        return CyclePrediction(
            predictedNext = predictedNext,
            averageCycleLength = avgLength,
            daysUntilNext = java.time.LocalDate.now().until(predictedNext).days.toLong()
        )
    }

    data class DashboardStats(
        val totalActivities: Int,
        val totalCycles: Int,
        val activeMedications: Int,
        val partnerCount: Int
    )

    data class CyclePrediction(
        val predictedNext: LocalDate,
        val averageCycleLength: Int,
        val daysUntilNext: Long
    )
}