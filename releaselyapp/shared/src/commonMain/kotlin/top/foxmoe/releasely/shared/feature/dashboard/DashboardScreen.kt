package top.foxmoe.releasely.shared.feature.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import top.foxmoe.releasely.shared.designsystem.components.ReleaselyCard
import top.foxmoe.releasely.shared.domain.model.DailyTrend

@Composable
fun DashboardScreen(
    streakDays: Int,
    weeklyTrend: List<DailyTrend>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ReleaselyCard {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("连续健康打卡", style = MaterialTheme.typography.bodyMedium)
                Text("$streakDays 天", style = MaterialTheme.typography.displayLarge)
            }
        }

        ReleaselyCard {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("本周状态", style = MaterialTheme.typography.bodyLarge)
                TrendChart(data = weeklyTrend)
            }
        }
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

