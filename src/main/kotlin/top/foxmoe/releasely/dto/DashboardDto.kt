package top.foxmoe.releasely.dto

data class DashboardResponse(
    val totalActivities: Int,
    val totalCycles: Int,
    val activeMedications: Int,
    val partnerCount: Int,
    val recentActivities: List<ActivityDto>,
    val cyclePrediction: CyclePredictionResponse?
)