package top.foxmoe.releasely.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.foxmoe.releasely.components.CalendarView
import top.foxmoe.releasely.components.DateMarker
import java.time.LocalDate

// Data classes for shared calendar events
data class SharedCalendarEvent(
    val id: Long,
    val userId: Long,
    val userName: String?,
    val eventType: String,
    val date: LocalDate,
    val label: String,
    val isOwnEvent: Boolean
)

data class SharedCalendarData(
    val myEvents: List<SharedCalendarEvent>,
    val partnerEvents: List<SharedCalendarEvent>,
    val myCyclePrediction: CyclePredictionData?,
    val partnerCyclePrediction: CyclePredictionData?
)

data class CyclePredictionData(
    val predictedNext: LocalDate?,
    val averageCycleLength: Int?,
    val daysUntilNext: Int?,
    val safeDays: List<LocalDate>,
    val fertileDays: List<LocalDate>
)

@Composable
fun SharedCalendarScreen(
    sharedCalendarData: SharedCalendarData?,
    isLoading: Boolean,
    onBack: () -> Unit,
    onToggleSharing: (Boolean) -> Unit,
    isSharingEnabled: Boolean
) {
    var showSharingToggle by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF9C27B0),
                            Color(0xFFE91E63)
                        )
                    )
                )
                .padding(top = 16.dp, bottom = 24.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = "返回",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "共享日历",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.CalendarMonth,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "查看彼此的周期日历",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFFE91E63))
            }
        } else if (sharedCalendarData == null) {
            // No sharing available
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF9C27B0).copy(alpha = 0.08f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.CalendarMonth,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = Color(0xFF9C27B0).copy(alpha = 0.5f)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "暂无可共享的日历",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "需要伴侣绑定并开启日历共享",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { showSharingToggle = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9C27B0))
                    ) {
                        Text("设置日历共享")
                    }
                }
            }
        } else {
            // Show shared calendar
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Legend
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        LegendItem(color = Color(0xFFE91E63), label = "我的生理期")
                        LegendItem(color = Color(0xFF9C27B0), label = "伴侣生理期")
                        LegendItem(color = Color(0xFF4CAF50), label = "安全期")
                        LegendItem(color = Color(0xFFFF9800), label = "危险期")
                    }
                }

                // Calendar view with both users' events
                item {
                    val markedDates = mutableMapOf<LocalDate, DateMarker>()

                    // Add my events
                    sharedCalendarData.myEvents.forEach { event ->
                        markedDates[event.date] = DateMarker(Color(0xFFE91E63), "我的周期")
                    }

                    // Add partner events
                    sharedCalendarData.partnerEvents.forEach { event ->
                        markedDates[event.date] = DateMarker(Color(0xFF9C27B0), "伴侣周期")
                    }

                    // Add safe days
                    sharedCalendarData.myCyclePrediction?.safeDays?.forEach { date ->
                        if (!markedDates.containsKey(date)) {
                            markedDates[date] = DateMarker(Color(0xFF4CAF50), "安全期")
                        }
                    }

                    // Add fertile days
                    sharedCalendarData.myCyclePrediction?.fertileDays?.forEach { date ->
                        if (!markedDates.containsKey(date)) {
                            markedDates[date] = DateMarker(Color(0xFFFF9800), "危险期")
                        }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        CalendarView(
                            markedDates = markedDates,
                            onDateClick = { date ->
                                // Handle date click if needed
                            }
                        )
                    }
                }

                // My cycle prediction
                sharedCalendarData.myCyclePrediction?.let { prediction ->
                    item {
                        PredictionCard(
                            title = "我的周期预测",
                            prediction = prediction,
                            color = Color(0xFFE91E63)
                        )
                    }
                }

                // Partner cycle prediction
                sharedCalendarData.partnerCyclePrediction?.let { prediction ->
                    item {
                        PredictionCard(
                            title = "伴侣周期预测",
                            prediction = prediction,
                            color = Color(0xFF9C27B0)
                        )
                    }
                }

                // Upcoming events
                val allEvents = (sharedCalendarData.myEvents + sharedCalendarData.partnerEvents)
                    .sortedBy { it.date }

                if (allEvents.isNotEmpty()) {
                    item {
                        Text(
                            text = "即将到来的事件",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    items(allEvents.take(5)) { event ->
                        EventCard(event = event)
                    }
                }
            }
        }

        // Sharing settings dialog
        if (showSharingToggle) {
            AlertDialog(
                onDismissRequest = { showSharingToggle = false },
                title = { Text("日历共享设置") },
                text = {
                    Column {
                        Text("开启后，伴侣可以看到你的周期开始日期、安全期和危险期预测。")
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("日历共享")
                            Spacer(modifier = Modifier.weight(1f))
                            Switch(
                                checked = isSharingEnabled,
                                onCheckedChange = {
                                    onToggleSharing(it)
                                    showSharingToggle = false
                                }
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showSharingToggle = false }) {
                        Text("关闭")
                    }
                }
            )
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PredictionCard(
    title: String,
    prediction: CyclePredictionData,
    color: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Favorite,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = color
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                PredictionItem(
                    label = "下次预测",
                    value = prediction.predictedNext?.toString() ?: "-",
                    color = color
                )
                PredictionItem(
                    label = "平均周期",
                    value = prediction.averageCycleLength?.toString() ?: "-",
                    suffix = "天",
                    color = color
                )
                PredictionItem(
                    label = "距下次",
                    value = if (prediction.daysUntilNext != null && prediction.daysUntilNext > 0) {
                        prediction.daysUntilNext.toString()
                    } else "-",
                    suffix = "天",
                    color = color
                )
            }
        }
    }
}

@Composable
private fun PredictionItem(
    label: String,
    value: String,
    suffix: String = "",
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = color.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            if (suffix.isNotEmpty()) {
                Text(
                    text = suffix,
                    fontSize = 11.sp,
                    color = color.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun EventCard(event: SharedCalendarEvent) {
    val color = if (event.isOwnEvent) Color(0xFFE91E63) else Color(0xFF9C27B0)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Favorite,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.label,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = color
                )
                Text(
                    text = event.date.toString(),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (!event.isOwnEvent && event.userName != null) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = color.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = event.userName,
                        fontSize = 11.sp,
                        color = color,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}
