package top.foxmoe.releasely.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.foxmoe.releasely.ReleaselyApp

/**
 * 首页：展示用户问候语、今日概览统计卡片和最近记录入口
 */
@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val app = context.applicationContext as ReleaselyApp

    // 统计数据状态
    var activityCount by remember { mutableIntStateOf(0) }
    var cycleDay by remember { mutableIntStateOf(0) }
    var cyclePhase by remember { mutableStateOf("") }
    var protectionRate by remember { mutableStateOf("0%") }

    // 加载统计数据
    LaunchedEffect(Unit) {
        activityCount = app.activityService.getActivityCount().toInt()
        val latestCycle = app.cycleService.getLatestCycle()
        if (latestCycle != null) {
            val now = System.currentTimeMillis() / 1000
            val daysSinceStart = ((now - latestCycle.startDate) / (24 * 60 * 60)).toInt()
            cycleDay = daysSinceStart
            cyclePhase = when {
                daysSinceStart < 5 -> "月经期"
                daysSinceStart < 14 -> "卵泡期"
                daysSinceStart < 21 -> "排卵期"
                else -> "黄体期"
            }
        }
    }

    val greeting = remember {
        val hour = java.time.LocalTime.now().hour
        when {
            hour < 6 -> "凌晨好"
            hour < 12 -> "早上好"
            hour < 18 -> "下午好"
            else -> "晚上好"
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 问候语区域
        item {
            Text(
                text = greeting,
                fontSize = 28.sp,
                fontWeight = FontWeight.Light,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "已记录 $activityCount 天",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }

        // 今日概览标题
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "今日概览",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
        }

        // 统计卡片第一行：周期天数 + 亲密次数
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "周期天数",
                    value = if (cycleDay > 0) "Day $cycleDay" else "-",
                    subtitle = cyclePhase.ifEmpty { "未记录" },
                    color = Color(0xFFE3F2FD),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "亲密次数",
                    value = activityCount.toString(),
                    subtitle = "累计",
                    color = Color(0xFFFCE4EC),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 统计卡片第二行：保护率 + 心情指数
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "保护率",
                    value = protectionRate,
                    subtitle = "本月",
                    color = Color(0xFFE8F5E9),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "心情指数",
                    value = "-",
                    subtitle = "今日",
                    color = Color(0xFFFFF3E0),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 最近记录区域
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "最近记录",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
                // TODO: 跳转至全部记录列表
                /*TextButton(onClick = { }) {
                    Text("查看全部", color = Color.Gray)
                }*/
            }
        }
    }
}

/**
 * 统计卡片组件，用于首页展示各项关键指标
 *
 * @param title 指标名称（如"周期天数"）
 * @param value 指标数值
 * @param subtitle 辅助说明文字
 * @param color 卡片背景色
 */
@Composable
fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = Color.Gray
            )
        }
    }
}
