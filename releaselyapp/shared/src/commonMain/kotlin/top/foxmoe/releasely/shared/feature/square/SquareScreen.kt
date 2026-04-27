package top.foxmoe.releasely.shared.feature.square

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private data class ToolItem(
    val title: String,
    val subtitle: String,
    val tint: Color
)

@Composable
fun SquareScreen(
    onToolClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val tools = listOf(
        ToolItem("情绪打分", "快速记录今日状态", Color(0xFF7E57C2)),
        ToolItem("生理提醒", "月经与护理提醒", Color(0xFFE53970)),
        ToolItem("体重曲线", "身体变化趋势", Color(0xFF00897B)),
        ToolItem("睡眠追踪", "休息质量统计", Color(0xFF1E88E5)),
        ToolItem("喝水计划", "每日补水目标", Color(0xFFF57C00)),
        ToolItem("私密笔记", "仅自己可见", Color(0xFF5D4037))
    )

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("健康广场", style = MaterialTheme.typography.displayLarge)
        Text("常用工具快捷入口", style = MaterialTheme.typography.bodyMedium)

        tools.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                row.forEach { tool ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(112.dp)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        tool.tint.copy(alpha = 0.2f),
                                        MaterialTheme.colorScheme.surface
                                    )
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { onToolClick(tool.title) }
                            .padding(12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(tool.title, style = MaterialTheme.typography.bodyLarge)
                            Text(tool.subtitle, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
                if (row.size == 1) {
                    Box(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

