package top.foxmoe.releasely.dto

import java.time.LocalDate

data class CalendarEventDto(
    val id: Long,
    val userId: Long,
    val userName: String?,
    val eventType: String,
    val date: LocalDate,
    val label: String,
    val isOwnEvent: Boolean
)

data class SharedCalendarResponse(
    val myEvents: List<CalendarEventDto>,
    val partnerEvents: List<CalendarEventDto>,
    val myCyclePrediction: CyclePredictionDto?,
    val partnerCyclePrediction: CyclePredictionDto?
)

data class CyclePredictionDto(
    val predictedNext: LocalDate?,
    val averageCycleLength: Int?,
    val daysUntilNext: Int?,
    val safeDays: List<LocalDate>,
    val fertileDays: List<LocalDate>
)

data class UpdateCalendarSharingRequest(
    val id: Long,
    val enabled: Boolean
)
