package top.foxmoe.releasely.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import top.foxmoe.releasely.dto.*
import top.foxmoe.releasely.service.DashboardService

@RestController
@RequestMapping("/api/dashboard")
class DashboardController(private val dashboardService: DashboardService) {

    @GetMapping
    fun getDashboard(@RequestParam userId: Long): ResponseEntity<ApiResponse<DashboardResponse>> {
        val stats = dashboardService.getStats(userId)
        val recentActivities = dashboardService.getRecentActivities(userId)
        val cyclePrediction = dashboardService.getCyclePrediction(userId)

        val recentActivityDtos = recentActivities.map { activity ->
            ActivityDto(
                id = activity.id,
                userId = activity.userId,
                type = activity.type,
                protection = activity.protection,
                pleasureRating = activity.pleasureRating,
                healthStatus = activity.healthStatus,
                occurredAt = activity.occurredAt,
                createdAt = activity.createdAt
            )
        }

        val cyclePredictionResponse = cyclePrediction?.let {
            CyclePredictionResponse(
                predictedNext = it.predictedNext,
                averageCycleLength = it.averageCycleLength,
                daysUntilNext = it.daysUntilNext
            )
        }

        val dashboard = DashboardResponse(
            totalActivities = stats.totalActivities,
            totalCycles = stats.totalCycles,
            activeMedications = stats.activeMedications,
            partnerCount = stats.partnerCount,
            recentActivities = recentActivityDtos,
            cyclePrediction = cyclePredictionResponse
        )

        return ResponseEntity.ok(ApiResponse.success(dashboard))
    }

    @GetMapping("/stats")
    fun getStats(@RequestParam userId: Long): ResponseEntity<ApiResponse<DashboardResponse>> {
        val stats = dashboardService.getStats(userId)
        val recentActivities = dashboardService.getRecentActivities(userId)
        val cyclePrediction = dashboardService.getCyclePrediction(userId)

        val recentActivityDtos = recentActivities.map { activity ->
            ActivityDto(
                id = activity.id,
                userId = activity.userId,
                type = activity.type,
                protection = activity.protection,
                pleasureRating = activity.pleasureRating,
                healthStatus = activity.healthStatus,
                occurredAt = activity.occurredAt,
                createdAt = activity.createdAt
            )
        }

        val response = DashboardResponse(
            totalActivities = stats.totalActivities,
            totalCycles = stats.totalCycles,
            activeMedications = stats.activeMedications,
            partnerCount = stats.partnerCount,
            recentActivities = recentActivityDtos,
            cyclePrediction = cyclePrediction?.let {
                CyclePredictionResponse(
                    predictedNext = it.predictedNext,
                    averageCycleLength = it.averageCycleLength,
                    daysUntilNext = it.daysUntilNext
                )
            }
        )

        return ResponseEntity.ok(ApiResponse.success(response))
    }
}