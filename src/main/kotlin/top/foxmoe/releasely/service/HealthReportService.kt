package top.foxmoe.releasely.service

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import top.foxmoe.releasely.dto.HealthInsight
import top.foxmoe.releasely.entity.ActivityRecord
import top.foxmoe.releasely.entity.Cycle
import top.foxmoe.releasely.entity.HealthReport
import top.foxmoe.releasely.mapper.ActivityRecordMapper
import top.foxmoe.releasely.mapper.CycleMapper
import top.foxmoe.releasely.mapper.HealthReportMapper
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Service
class HealthReportService(
    private val healthReportMapper: HealthReportMapper,
    private val activityRecordMapper: ActivityRecordMapper,
    private val cycleMapper: CycleMapper
) {

    @Value("\${AI_API_KEY:}")
    private lateinit var aiApiKey: String

    @Value("\${AI_PROVIDER:claude}")
    private lateinit var aiProvider: String

    @Value("\${AI_MODEL:claude-sonnet-4-20250514}")
    private lateinit var aiModel: String

    fun getReportsByUserId(userId: Long): List<HealthReport> {
        val wrapper = QueryWrapper<HealthReport>()
            .eq("user_id", userId)
            .orderByDesc("created_at")
        return healthReportMapper.selectList(wrapper)
    }

    fun getReportById(id: Long): HealthReport? {
        return healthReportMapper.selectById(id)
    }

    fun getReportByPeriod(userId: Long, period: String): HealthReport? {
        val wrapper = QueryWrapper<HealthReport>()
            .eq("user_id", userId)
            .eq("period", period)
        return healthReportMapper.selectOne(wrapper)
    }

    fun createReport(report: HealthReport): Long {
        report.createdAt = LocalDateTime.now()
        healthReportMapper.insert(report)
        return report.id ?: 0L
    }

    fun updateReport(report: HealthReport): Boolean {
        return healthReportMapper.updateById(report) > 0
    }

    fun deleteReport(id: Long): Boolean {
        return healthReportMapper.deleteById(id) > 0
    }

    fun generateReport(userId: Long, days: Long, periodType: String): HealthReport {
        val endDate = LocalDateTime.now()
        val startDate = endDate.minusDays(days)

        val wrapper = QueryWrapper<ActivityRecord>()
            .eq("user_id", userId)
            .ge("occurred_at", startDate)
            .le("occurred_at", endDate)

        val records = activityRecordMapper.selectList(wrapper)
        val frequencyData = buildFrequencyData(records)

        val report = HealthReport(
            userId = userId,
            period = "${periodType}_${endDate.format(DateTimeFormatter.ISO_DATE)}",
            frequencyData = frequencyData.first,
            protectionRate = frequencyData.second
        )

        createReport(report)
        return report
    }

    private fun buildFrequencyData(records: List<ActivityRecord>): Pair<String, Double> {
        val totalActivities = records.size
        val protectedActivities = records.count { r ->
            val p = r.protection
            p != null && p.isNotEmpty()
        }
        val protectionRate = if (totalActivities > 0) {
            (protectedActivities.toDouble() / totalActivities) * 100
        } else {
            0.0
        }

        val frequencyData = """
            {
                "total": $totalActivities,
                "protected": $protectedActivities,
                "unprotected": ${totalActivities - protectedActivities},
                "averagePleasure": ${records.mapNotNull { it.pleasureRating }.average().takeIf { !it.isNaN() } ?: 0}
            }
        """.trimIndent()

        return Pair(frequencyData, protectionRate)
    }

    fun generateWeeklyReport(userId: Long): HealthReport {
        return generateReport(userId, 7, "weekly")
    }

    fun generateMonthlyReport(userId: Long): HealthReport {
        return generateReport(userId, 30, "monthly")
    }

    /**
     * 生成 AI 健康建议
     * 当前使用规则引擎生成，未来可对接 Claude/OpenAI API
     *
     * AI 对接预留接口：
     *
     * // Claude API 调用示例
     * fun generateAIInsightsFromLLM(reportId: Long): List<HealthInsight> {
     *     val report = getReportById(reportId) ?: return emptyList()
     *     val prompt = buildInsightPrompt(report)
     *
     *     val requestBody = mapOf(
     *         "model" to aiModel,
     *         "messages" to listOf(
     *             mapOf("role" to "user", "content" to prompt)
     *         ),
     *         "max_tokens" to 1000
     *     )
     *
     *     val restTemplate = RestTemplate()
     *     val headers = mapOf(
     *         "x-api-key" to aiApiKey,
     *         "anthropic-version" to "2023-06-01",
     *         "Content-Type" to "application/json"
     *     )
     *
     *     // POST to https://api.anthropic.com/v1/messages (Claude API)
     *     // 或根据 aiProvider 切换到 OpenAI 等其他 provider
     *     return emptyList() // 解析 LLM 响应
     * }
     *
     * // OpenAI API 调用示例
     * fun generateAIInsightsFromOpenAI(reportId: Long): List<HealthInsight> {
     *     val report = getReportById(reportId) ?: return emptyList()
     *     val prompt = buildInsightPrompt(report)
     *
     *     val requestBody = mapOf(
     *         "model" to aiModel,
     *         "messages" to listOf(
     *             mapOf("role" to "user", "content" to prompt)
     *         ),
     *         "max_tokens" to 1000
     *     )
     *
     *     val restTemplate = RestTemplate()
     *     val headers = mapOf(
     *         "Authorization" to "Bearer $aiApiKey",
     *         "Content-Type" to "application/json"
     *     )
     *
     *     // POST to https://api.openai.com/v1/chat/completions (OpenAI API)
     *     return emptyList()
     * }
     */
    fun generateAIInsights(reportId: Long): List<HealthInsight> {
        val report = getReportById(reportId) ?: return emptyList()
        val insights = mutableListOf<HealthInsight>()

        // 解析 frequencyData
        val totalActivities: Int
        val protectedActivities: Int
        val unprotectedActivities: Int
        val averagePleasure: Double

        try {
            val data = com.fasterxml.jackson.databind.ObjectMapper().readTree(report.frequencyData)
            totalActivities = data.get("total")?.asInt() ?: 0
            protectedActivities = data.get("protected")?.asInt() ?: 0
            unprotectedActivities = data.get("unprotected")?.asInt() ?: 0
            averagePleasure = data.get("averagePleasure")?.asDouble() ?: 0.0
        } catch (e: Exception) {
            return emptyList()
        }

        // 获取用户周期数据
        val cycles = getUserCycles(report.userId ?: return emptyList())
        val cycleRegularity = analyzeCycleRegularity(cycles)

        // 规则1: 保护率低于50%
        if ((report.protectionRate ?: 0.0) < 50.0 && totalActivities > 0) {
            insights.add(
                HealthInsight(
                    insightText = "您的保护措施使用率较低（${String.format("%.1f", report.protectionRate)}%），建议增加保护措施的使用频率，以降低健康风险。",
                    category = "protection",
                    priority = 1
                )
            )
        }

        // 规则2: 保护率在50%-80%之间
        if ((report.protectionRate ?: 0.0) in 50.0..80.0 && totalActivities > 0) {
            insights.add(
                HealthInsight(
                    insightText = "保护措施使用率（${String.format("%.1f", report.protectionRate)}%）有提升空间，建议继续关注并增加使用频率。",
                    category = "protection",
                    priority = 2
                )
            )
        }

        // 规则3: 无保护活动超过3次
        if (unprotectedActivities > 3) {
            insights.add(
                HealthInsight(
                    insightText = "本期记录中有 ${unprotectedActivities} 次未使用保护措施的活动，建议留意身体健康，必要时咨询医生。",
                    category = "protection",
                    priority = 1
                )
            )
        }

        // 规则4: 周期不规律
        if (cycleRegularity == "irregular") {
            insights.add(
                HealthInsight(
                    insightText = "您的月经周期似乎不够规律，建议记录更多周期数据或咨询妇科医生进行专业评估。",
                    category = "cycle",
                    priority = 1
                )
            )
        }

        // 规则5: 周期略有不规律
        if (cycleRegularity == "slightly_irregular") {
            insights.add(
                HealthInsight(
                    insightText = "您的周期存在轻微波动，建议继续保持记录，关注身体变化。",
                    category = "cycle",
                    priority = 2
                )
            )
        }

        // 规则6: 总记录数很少
        if (totalActivities < 3) {
            insights.add(
                HealthInsight(
                    insightText = "您目前的健康记录较少，建议坚持规律记录，以便获得更准确的分析和建议。",
                    category = "general",
                    priority = 3
                )
            )
        }

        // 规则7: 愉悦度较高
        if (averagePleasure >= 4.0 && totalActivities >= 3) {
            insights.add(
                HealthInsight(
                    insightText = "您近期的健康状态不错，愉悦度评分较高（${String.format("%.1f", averagePleasure)}），继续保持良好生活习惯。",
                    category = "general",
                    priority = 3
                )
            )
        }

        // 规则8: 保护率很高(>=90%)
        if ((report.protectionRate ?: 0.0) >= 90.0 && totalActivities > 0) {
            insights.add(
                HealthInsight(
                    insightText = "您的保护措施使用率很高（${String.format("%.1f", report.protectionRate)}%），继续保持！",
                    category = "protection",
                    priority = 3
                )
            )
        }

        // 规则9: 周期天数异常（过短或过长）
        val avgCycleLength = calculateAverageCycleLength(cycles)
        if (avgCycleLength > 0 && (avgCycleLength < 21 || avgCycleLength > 35)) {
            insights.add(
                HealthInsight(
                    insightText = "您的平均周期长度（${avgCycleLength}天）偏离正常范围（21-35天），建议咨询医生。",
                    category = "cycle",
                    priority = 1
                )
            )
        }

        // 按优先级排序并限制返回3-5条
        return insights.sortedBy { it.priority }.take(5)
    }

    private fun getUserCycles(userId: Long): List<Cycle> {
        val wrapper = QueryWrapper<Cycle>()
            .eq("user_id", userId)
            .orderByDesc("start_date")
            .last("LIMIT 12")
        return cycleMapper.selectList(wrapper)
    }

    private fun analyzeCycleRegularity(cycles: List<Cycle>): String {
        if (cycles.size < 3) return "insufficient_data"

        val durations = mutableListOf<Int>()
        val sortedCycles = cycles.sortedBy { it.startDate }

        for (i in 1 until sortedCycles.size) {
            val prev = sortedCycles[i - 1].startDate
            val curr = sortedCycles[i].startDate
            if (prev != null && curr != null) {
                durations.add(ChronoUnit.DAYS.between(prev, curr).toInt())
            }
        }

        if (durations.isEmpty()) return "insufficient_data"

        val avg = durations.average()
        val variance = durations.map { (it - avg) * (it - avg) }.average()
        val stdDev = kotlin.math.sqrt(variance)

        return when {
            stdDev > 7 -> "irregular"
            stdDev > 4 -> "slightly_irregular"
            else -> "regular"
        }
    }

    private fun calculateAverageCycleLength(cycles: List<Cycle>): Int {
        if (cycles.size < 2) return 0

        val durations = mutableListOf<Int>()
        val sortedCycles = cycles.sortedBy { it.startDate }

        for (i in 1 until sortedCycles.size) {
            val prev = sortedCycles[i - 1].startDate
            val curr = sortedCycles[i].startDate
            if (prev != null && curr != null) {
                durations.add(ChronoUnit.DAYS.between(prev, curr).toInt())
            }
        }

        return if (durations.isNotEmpty()) durations.average().toInt() else 0
    }
}