package top.foxmoe.releasely.services

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

/**
 * 健康报告服务：对接后端 /api/health-reports/generate API
 * 登录状态下调用后端生成报告，未登录时返回 null（由调用方回退到本地统计）
 */
class HealthReportService(private val apiService: ApiService) {

    /**
     * 向后端请求生成健康报告
     * @param userId 当前登录用户 ID
     * @param reportType "weekly" 或 "monthly"
     * @return 后端返回的报告文本摘要，失败返回 null
     */
    suspend fun generateReport(userId: Long, reportType: String = "weekly"): String? = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("userId", userId)
                put("reportType", reportType)
            }
            val response = apiService.post("/health-reports/generate", json.toString())
            response.getOrNull()?.let { jsonString ->
                val obj = JSONObject(jsonString)
                val code = obj.optInt("code", 500)
                if (code == 200) {
                    val data = obj.optJSONObject("data")
                    val period = data?.optString("period", "")
                    val protectionRate = data?.optDouble("protectionRate", 0.0) ?: 0.0
                    val frequencyData = data?.optString("frequencyData", "{}") ?: "{}"
                    val fd = JSONObject(frequencyData)
                    val total = fd.optInt("total", 0)
                    val protected = fd.optInt("protected", 0)
                    val unprotected = fd.optInt("unprotected", 0)
                    val avgPleasure = fd.optDouble("averagePleasure", 0.0)

                    buildString {
                        appendLine("=== 健康报告 ===")
                        appendLine("统计周期：$period")
                        appendLine("总记录数：$total")
                        appendLine("有保护：$protected")
                        appendLine("无保护：$unprotected")
                        appendLine("保护措施率：${String.format("%.1f", protectionRate)}%")
                        if (avgPleasure > 0) {
                            appendLine("平均愉悦度：${String.format("%.1f", avgPleasure)}")
                        }
                    }
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            null
        }
    }
}
