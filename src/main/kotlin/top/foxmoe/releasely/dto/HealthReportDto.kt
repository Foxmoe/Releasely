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

data class HealthReportListResponse(
    val reports: List<HealthReportDto>,
    val total: Int
)

/**
 * 健康建议 DTO
 * 用于返回基于规则的 AI 健康建议
 */
data class HealthInsight(
    val insightText: String,
    val category: String,      // "protection" | "cycle" | "medication" | "general"
    val priority: Int         // 1=高, 2=中, 3=低
)

enum class ReportType {
    WEEKLY,
    MONTHLY
}