package top.foxmoe.releasely.service

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import org.springframework.stereotype.Service
import top.foxmoe.releasely.entity.Cycle
import top.foxmoe.releasely.mapper.CycleMapper
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Service
class CycleService(private val cycleMapper: CycleMapper) {

    fun getCyclesByUserId(userId: Long): List<Cycle> {
        val wrapper = QueryWrapper<Cycle>()
            .eq("user_id", userId)
            .eq("is_deleted", false)
            .orderByDesc("start_date")
        return cycleMapper.selectList(wrapper)
    }

    fun getCycleById(id: Long): Cycle? {
        return cycleMapper.selectById(id)
    }

    fun createCycle(cycle: Cycle): Long {
        cycle.createdAt = LocalDateTime.now()
        cycle.isDeleted = false
        cycleMapper.insert(cycle)
        return cycle.id ?: 0L
    }

    fun updateCycle(cycle: Cycle): Boolean {
        return cycleMapper.updateById(cycle) > 0
    }

    fun deleteCycle(id: Long): Boolean {
        val cycle = cycleMapper.selectById(id)
        if (cycle != null) {
            cycle.isDeleted = true
            return cycleMapper.updateById(cycle) > 0
        }
        return false
    }

    fun predictNextPeriod(userId: Long): LocalDate? {
        val cycles = getCyclesByUserId(userId)
        if (cycles.size < 2) {
            return null
        }

        val sortedCycles = cycles.sortedByDescending { it.startDate }
        val latestCycle = sortedCycles.firstOrNull() ?: return null
        val previousCycle = sortedCycles.getOrNull(1) ?: return null

        val latestStart = latestCycle.startDate ?: return null
        val previousStart = previousCycle.startDate ?: return null

        val averageCycleLength = ChronoUnit.DAYS.between(previousStart, latestStart)
        val nextPredicted = latestStart.plusDays(averageCycleLength)

        latestCycle.predictedNext = nextPredicted
        updateCycle(latestCycle)

        return nextPredicted
    }

    fun calculateAverageCycleLength(userId: Long): Int? {
        val cycles = getCyclesByUserId(userId)
        if (cycles.size < 2) {
            return cycles.firstOrNull()?.duration
        }

        val sortedCycles = cycles.sortedByDescending { it.startDate }
        val latestCycle = sortedCycles.firstOrNull() ?: return null
        val previousCycle = sortedCycles.getOrNull(1) ?: return null

        val latestStart = latestCycle.startDate ?: return null
        val previousStart = previousCycle.startDate ?: return null

        return ChronoUnit.DAYS.between(previousStart, latestStart).toInt()
    }
}