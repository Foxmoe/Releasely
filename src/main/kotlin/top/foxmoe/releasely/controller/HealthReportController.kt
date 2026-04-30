package top.foxmoe.releasely.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import top.foxmoe.releasely.annotation.AuditLog
import top.foxmoe.releasely.dto.*
import top.foxmoe.releasely.entity.HealthReport
import top.foxmoe.releasely.service.HealthReportService

@RestController
@RequestMapping("/api/health-reports")
class HealthReportController(
    private val healthReportService: HealthReportService,
    private val objectMapper: ObjectMapper
) {

    @GetMapping
    fun getReports(@RequestParam userId: Long): ResponseEntity<ApiResponse<List<HealthReport>>> {
        val reports = healthReportService.getReportsByUserId(userId)
        return ResponseEntity.ok(ApiResponse.success(reports))
    }

    @GetMapping("/{id}")
    fun getReportById(@PathVariable id: Long): ResponseEntity<ApiResponse<HealthReport>> {
        val report = healthReportService.getReportById(id)
            ?: return ResponseEntity.ok(ApiResponse.error(ResultCode.NOT_FOUND))
        return ResponseEntity.ok(ApiResponse.success(report))
    }

    @GetMapping("/period/{period}")
    fun getReportByPeriod(
        @RequestParam userId: Long,
        @PathVariable period: String
    ): ResponseEntity<ApiResponse<HealthReport>> {
        val report = healthReportService.getReportByPeriod(userId, period)
            ?: return ResponseEntity.ok(ApiResponse.error(ResultCode.NOT_FOUND))
        return ResponseEntity.ok(ApiResponse.success(report))
    }

    @AuditLog(action = "HEALTH_REPORT_GENERATE", resourceType = "HEALTH_REPORT")
    @PostMapping("/generate")
    fun generateReport(@RequestBody request: GenerateReportRequest): ResponseEntity<ApiResponse<HealthReport>> {
        val report = when (request.reportType.lowercase()) {
            "weekly" -> healthReportService.generateWeeklyReport(request.userId)
            "monthly" -> healthReportService.generateMonthlyReport(request.userId)
            else -> return ResponseEntity.ok(ApiResponse.error(ResultCode.VALIDATE_FAILED))
        }
        return ResponseEntity.ok(ApiResponse.success(report))
    }

    @AuditLog(action = "HEALTH_REPORT_CREATE", resourceType = "HEALTH_REPORT")
    @PostMapping
    fun createReport(@RequestBody request: CreateHealthReportRequest): ResponseEntity<ApiResponse<HealthReport>> {
        val report = HealthReport(
            userId = request.userId,
            period = request.period,
            frequencyData = request.frequencyData,
            protectionRate = request.protectionRate
        )
        val id = healthReportService.createReport(report)
        val createdReport = healthReportService.getReportById(id)
        return ResponseEntity.ok(ApiResponse.success(createdReport))
    }

    @AuditLog(action = "HEALTH_REPORT_UPDATE", resourceType = "HEALTH_REPORT")
    @PutMapping("/{id}")
    fun updateReport(
        @PathVariable id: Long,
        @RequestBody request: UpdateHealthReportRequest
    ): ResponseEntity<ApiResponse<HealthReport>> {
        val existingReport = healthReportService.getReportById(id)
            ?: return ResponseEntity.ok(ApiResponse.error(ResultCode.NOT_FOUND))

        request.period?.let { existingReport.period = it }
        request.frequencyData?.let { existingReport.frequencyData = it }
        request.protectionRate?.let { existingReport.protectionRate = it }

        healthReportService.updateReport(existingReport)
        return ResponseEntity.ok(ApiResponse.success(existingReport))
    }

    @AuditLog(action = "HEALTH_REPORT_DELETE", resourceType = "HEALTH_REPORT")
    @DeleteMapping("/{id}")
    fun deleteReport(@PathVariable id: Long): ResponseEntity<ApiResponse<Nothing>> {
        val success = healthReportService.deleteReport(id)
        return if (success) {
            ResponseEntity.ok(ApiResponse.success("Report deleted"))
        } else {
            ResponseEntity.ok(ApiResponse.error(ResultCode.NOT_FOUND))
        }
    }

    @GetMapping("/summary/{id}")
    fun getReportSummary(@PathVariable id: Long): ResponseEntity<ApiResponse<ReportSummaryResponse>> {
        val report = healthReportService.getReportById(id)
            ?: return ResponseEntity.ok(ApiResponse.error(ResultCode.NOT_FOUND))

        val data = try {
            objectMapper.readTree(report.frequencyData)
        } catch (e: Exception) {
            return ResponseEntity.ok(ApiResponse.error(ResultCode.INTERNAL_ERROR))
        }

        val summary = ReportSummaryResponse(
            totalActivities = data.get("total")?.asInt() ?: 0,
            protectedActivities = data.get("protected")?.asInt() ?: 0,
            unprotectedActivities = data.get("unprotected")?.asInt() ?: 0,
            protectionRate = report.protectionRate ?: 0.0,
            averagePleasure = data.get("averagePleasure")?.asDouble() ?: 0.0,
            period = report.period ?: ""
        )

        return ResponseEntity.ok(ApiResponse.success(summary))
    }

    @GetMapping("/{id}/insights")
    fun getHealthInsights(@PathVariable id: Long): ResponseEntity<ApiResponse<List<HealthInsight>>> {
        val report = healthReportService.getReportById(id)
            ?: return ResponseEntity.ok(ApiResponse.error(ResultCode.NOT_FOUND))

        val insights = healthReportService.generateAIInsights(id)
        return ResponseEntity.ok(ApiResponse.success(insights))
    }
}
