package top.foxmoe.releasely.screens.tabs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.foxmoe.releasely.ReleaselyApp
import top.foxmoe.releasely.components.CalendarView
import top.foxmoe.releasely.components.DateMarker
import top.foxmoe.releasely.utils.formatDate
import java.time.Instant
import java.time.ZoneId

/**
 * 周期记录 Tab：展示日历视图和月经周期历史记录
 */
@Composable
fun CycleTab(onAddClick: () -> Unit) {
    val context = LocalContext.current
    val app = context.applicationContext as ReleaselyApp

    var cycles by remember { mutableStateOf<List<top.foxmoe.releasely.services.CycleRecord>>(emptyList()) }
    var activities by remember { mutableStateOf<List<top.foxmoe.releasely.services.ActivityRecord>>(emptyList()) }

    LaunchedEffect(Unit) {
        cycles = app.cycleService.getAllCycles()
        activities = app.activityService.getAllActivities()
    }

    // 将行为记录日期映射为日历标记
    val markedDates = remember(activities) {
        activities.associate { activity ->
            val instant = Instant.ofEpochSecond(activity.date)
            val date = instant.atZone(ZoneId.systemDefault()).toLocalDate()
            date to DateMarker(
                color = Color(0xFF9C27B0),
                label = activity.type
            )
        }
    }

    // 获取最新周期信息用于日历高亮
    val latestCycle = cycles.firstOrNull()
    val cycleStartDate = latestCycle?.let {
        Instant.ofEpochSecond(it.startDate)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
    }
    val cycleDuration = latestCycle?.duration ?: 5

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 日历视图：标记行为记录和月经周期
        item {
            CalendarView(
                markedDates = markedDates,
                cycleStartDate = cycleStartDate,
                cycleDuration = cycleDuration,
                onDateClick = { /* TODO: 点击日期展示当日详情 */ }
            )
        }

        // 添加周期记录按钮
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Black),
                onClick = onAddClick
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "添加",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "记录月经",
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // 历史周期记录列表
        items(cycles.size) { index ->
            val cycle = cycles[index]
            CycleRecordItem(
                startDate = formatDate(cycle.startDate),
                duration = cycle.duration?.toString() ?: "-",
                symptoms = ""
            )
        }
    }
}

/**
 * 单条周期记录卡片，展示开始日期和持续时间
 */
@Composable
private fun CycleRecordItem(
    startDate: String,
    duration: String,
    symptoms: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "月经期",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "开始于 $startDate",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(text = "${duration}天", fontSize = 14.sp, color = Color.Black)
                Text(text = "持续时间", fontSize = 10.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                if (symptoms.isNotEmpty()) {
                    Text(text = symptoms, fontSize = 11.sp, color = Color(0xFFE91E63))
                }
            }
        }
    }
}
