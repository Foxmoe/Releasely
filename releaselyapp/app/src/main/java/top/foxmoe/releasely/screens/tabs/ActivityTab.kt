package top.foxmoe.releasely.screens.tabs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
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
import top.foxmoe.releasely.components.common.Chip
import top.foxmoe.releasely.utils.formatDate

/**
 * 行为记录 Tab：展示亲密行为记录列表，支持添加新记录
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityTab(onAddClick: () -> Unit) {
    val context = LocalContext.current
    val app = context.applicationContext as ReleaselyApp

    var activities by remember { mutableStateOf<List<top.foxmoe.releasely.services.ActivityRecord>>(emptyList()) }

    LaunchedEffect(Unit) {
        activities = app.activityService.getAllActivities()
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 添加按钮
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
                        text = "添加新记录",
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // 记录列表
        items(activities.size) { index ->
            val activity = activities[index]
            RecordItem(
                date = formatDate(activity.date),
                type = activity.type,
                protection = if (activity.protection) "有保护" else "无保护",
                mood = activity.mood ?: "-"
            )
        }
    }
}

/**
 * 单条行为记录卡片，展示类型、日期、保护措施和心情
 */
@Composable
private fun RecordItem(
    date: String,
    type: String,
    protection: String,
    mood: String
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
                    text = type,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = date,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Chip(text = protection)
                Chip(text = mood)
            }
        }
    }
}
