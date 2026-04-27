package top.foxmoe.releasely.shared.feature.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import top.foxmoe.releasely.shared.domain.model.BiologicalSex
import top.foxmoe.releasely.shared.domain.model.DailyTrend

@Composable
fun DashboardScreen(
    biologicalSex: BiologicalSex,
    streakDays: Int,
    recentSexCount: Int,
    cycleDay: Int?,
    nextPeriodInDays: Int?,
    weeklyTrend: List<DailyTrend>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(top = 4.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.85f)
                        )
                    ),
                    shape = RoundedCornerShape(14.dp)
                )
                .padding(18.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("今日健康总览", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onPrimary)
                Text("$streakDays 天连续记录", style = MaterialTheme.typography.displayLarge, color = MaterialTheme.colorScheme.onPrimary)
                Text("近7天性生活记录 $recentSexCount 次", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimary)
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            MetricBlock(title = "活跃周", value = "$recentSexCount 次", modifier = Modifier.weight(1f))
            MetricBlock(title = "连续天数", value = "$streakDays 天", modifier = Modifier.weight(1f))
        }

        if (biologicalSex == BiologicalSex.FEMALE) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                MetricBlock(
                    title = "当前周期",
                    value = cycleDay?.let { "第 $it 天" } ?: "暂无",
                    modifier = Modifier.weight(1f)
                )
                MetricBlock(
                    title = "下次月经",
                    value = nextPeriodInDays?.let { "约 $it 天" } ?: "待记录",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("本周体验趋势", style = MaterialTheme.typography.bodyLarge)
            TrendChart(data = weeklyTrend)
        }
    }
}

@Composable
private fun MetricBlock(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(title, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun TrendChart(data: List<DailyTrend>) {
    if (data.isEmpty()) {
        Text("暂无趋势数据", style = MaterialTheme.typography.bodyMedium)
        return
    }

    val max = (data.maxOfOrNull { it.score } ?: 1).coerceAtLeast(1)
    val lineColor = MaterialTheme.colorScheme.primary
    val pointColor = MaterialTheme.colorScheme.secondary
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
    ) {
        val stepX = size.width / (data.size - 1).coerceAtLeast(1)
        val path = Path()
        data.forEachIndexed { index, point ->
            val x = index * stepX
            val y = size.height - (point.score / max.toFloat()) * size.height
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 6f)
        )

        data.forEachIndexed { index, point ->
            val x = index * stepX
            val y = size.height - (point.score / max.toFloat()) * size.height
            drawCircle(
                color = pointColor,
                radius = 6f,
                center = Offset(x, y)
            )
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        data.forEach {
            Text(text = it.dayLabel, style = MaterialTheme.typography.labelSmall)
        }
    }
}

