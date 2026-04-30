package top.foxmoe.releasely.service

import org.springframework.stereotype.Service
import top.foxmoe.releasely.dto.CalendarEventDto
import top.foxmoe.releasely.dto.CyclePredictionDto
import top.foxmoe.releasely.dto.SharedCalendarResponse
import top.foxmoe.releasely.entity.Cycle
import top.foxmoe.releasely.mapper.CycleMapper
import top.foxmoe.releasely.mapper.UserMapper
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Service
class CalendarService(
    private val partnerService: PartnerService,
    private val cycleService: CycleService,
    private val cycleMapper: CycleMapper,
    private val userMapper: UserMapper
) {

    fun getSharedCalendar(userId: Long): SharedCalendarResponse? {
        val partnerRelation = partnerService.getAcceptedPartnerRelation(userId)
            ?: return null

        val partnerId = partnerRelation.partnerId ?: return null

        // Check if calendar sharing is enabled from both sides
        val reverseRelation = partnerService.getPartnerRelation(partnerId, userId)
        if (reverseRelation?.calendarSharingEnabled != true) {
            return null
        }

        // Get user's own cycles and events
        val myCycles = cycleService.getCyclesByUserId(userId)
        val myEvents = buildCycleEvents(myCycles, userId, isOwn = true)
        val myPrediction = buildCyclePrediction(userId)

        // Get partner's cycles and events
        val partnerCycles = cycleService.getCyclesByUserId(partnerId)
        val partnerEvents = buildCycleEvents(partnerCycles, partnerId, isOwn = false)
        val partnerPrediction = buildCyclePrediction(partnerId)

        return SharedCalendarResponse(
            myEvents = myEvents,
            partnerEvents = partnerEvents,
            myCyclePrediction = myPrediction,
            partnerCyclePrediction = partnerPrediction
        )
    }

    fun getCalendarEvents(userId: Long): List<CalendarEventDto> {
        val partnerRelation = partnerService.getAcceptedPartnerRelation(userId)
            ?: return emptyList()

        val partnerId = partnerRelation.partnerId ?: return emptyList()

        // Check if calendar sharing is enabled
        val reverseRelation = partnerService.getPartnerRelation(partnerId, userId)
        if (reverseRelation?.calendarSharingEnabled != true) {
            return emptyList()
        }

        val myCycles = cycleService.getCyclesByUserId(userId)
        val partnerCycles = cycleService.getCyclesByUserId(partnerId)

        val myEvents = buildCycleEvents(myCycles, userId, isOwn = true)
        val partnerEvents = buildCycleEvents(partnerCycles, partnerId, isOwn = false)

        return myEvents + partnerEvents
    }

    private fun buildCycleEvents(cycles: List<Cycle>, userId: Long, isOwn: Boolean): List<CalendarEventDto> {
        val userName = try {
            val userWrapper = QueryWrapper<top.foxmoe.releasely.entity.User>()
                .eq("id", userId)
            userMapper.selectOne(userWrapper)?.username
        } catch (e: Exception) {
            null
        }

        return cycles.mapNotNull { cycle ->
            cycle.startDate?.let { startDate ->
                CalendarEventDto(
                    id = cycle.id ?: 0L,
                    userId = userId,
                    userName = userName,
                    eventType = "PERIOD_START",
                    date = startDate,
                    label = if (isOwn) "生理期开始" else "$userName 的生理期开始",
                    isOwnEvent = isOwn
                )
            }
        }
    }

    private fun buildCyclePrediction(userId: Long): CyclePredictionDto? {
        val cycles = cycleService.getCyclesByUserId(userId)
        if (cycles.size < 2) {
            return null
        }

        val sortedCycles = cycles.sortedByDescending { it.startDate }
        val latestCycle = sortedCycles.firstOrNull() ?: return null
        val previousCycle = sortedCycles.getOrNull(1) ?: return null

        val latestStart = latestCycle.startDate ?: return null
        val previousStart = previousCycle.startDate ?: return null

        val cycleLength = ChronoUnit.DAYS.between(previousStart, latestStart).toInt()
        // duration is used below to set luteal phase length if available
        val duration = latestCycle.duration ?: 5

        val predictedNext = latestStart.plusDays(cycleLength.toLong())
        val daysUntilNext = ChronoUnit.DAYS.between(LocalDate.now(), predictedNext).toInt()

        // Calculate safe days and fertile days
        val ovulationDay = latestStart.plusDays((cycleLength / 2).toLong() - 2)
        val fertileStart = ovulationDay.minusDays(5)
        val fertileEnd = ovulationDay.plusDays(1)
        val safeDays = mutableListOf<LocalDate>()

        // First half of cycle: mostly safe (except right before fertile window)
        val cycleStart = latestStart
        for (i in 0 until ChronoUnit.DAYS.between(cycleStart, fertileStart).toInt()) {
            safeDays.add(cycleStart.plusDays(i.toLong()))
        }

        // Second half of cycle: safe after fertile window
        val lutealPhaseEnd = predictedNext
        for (i in ChronoUnit.DAYS.between(fertileEnd, lutealPhaseEnd).toInt() until cycleLength) {
            safeDays.add(latestStart.plusDays(i.toLong()))
        }

        val fertileDays = mutableListOf<LocalDate>()
        for (i in 0 until ChronoUnit.DAYS.between(fertileStart, fertileEnd).toInt()) {
            fertileDays.add(fertileStart.plusDays(i.toLong()))
        }

        return CyclePredictionDto(
            predictedNext = predictedNext,
            averageCycleLength = cycleLength,
            daysUntilNext = daysUntilNext,
            safeDays = safeDays,
            fertileDays = fertileDays
        )
    }
}
