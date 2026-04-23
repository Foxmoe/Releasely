package top.foxmoe.releasely.dto

import java.time.LocalDate
import java.time.LocalDateTime

data class CycleDto(
    val id: Long? = null,
    val userId: Long? = null,
    val startDate: LocalDate? = null,
    val duration: Int? = null,
    val predictedNext: LocalDate? = null,
    val averageCycleLength: Int? = null,
    val createdAt: LocalDateTime? = null
)

data class CreateCycleRequest(
    val userId: Long,
    val startDate: LocalDate,
    val duration: Int
)

data class UpdateCycleRequest(
    val id: Long,
    val startDate: LocalDate? = null,
    val duration: Int? = null
)

data class CyclePredictionResponse(
    val predictedNext: LocalDate?,
    val averageCycleLength: Int?,
    val daysUntilNext: Long?
)