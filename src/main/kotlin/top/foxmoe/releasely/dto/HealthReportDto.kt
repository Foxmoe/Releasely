package top.foxmoe.releasely.dto

import java.time.LocalDateTime

data class HealthReportDto(
    val id: Long? = null,
    val userId: Long? = null,
    val period: String? = null,
    val frequencyData: String? = null,
    val protectionRate: Double? = null,
    val createdAt: LocalDateTime? = null
)

data class CreateHealthReportRequest(
    val userId: Long,
    val period: String,
    val frequencyData: String,
    val protectionRate: Double
)

data class UpdateHealthReportRequest(
    val id: Long,
    val period: String? = null,
    val frequencyData: String? = null,
    val protectionRate: Double? = null
)

data class GenerateReportRequest(
    val userId: Long,
    val reportType: String
)

data class ReportSummaryResponse(
    val totalActivities: Int,
    val protectedActivities: Int,
    val unprotectedActivities: Int,
    val protectionRate: Double,
    val averagePleasure: Double,
    val period: String
)