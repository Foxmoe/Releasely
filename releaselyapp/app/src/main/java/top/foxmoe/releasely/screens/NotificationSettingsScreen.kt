package top.foxmoe.releasely.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NotificationSettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("notifications", Context.MODE_PRIVATE) }

    var medicationReminder by remember { mutableStateOf(prefs.getBoolean("medication_reminder", true)) }
    var cycleReminder by remember { mutableStateOf(prefs.getBoolean("cycle_reminder", true)) }
    var dailySummary by remember { mutableStateOf(prefs.getBoolean("daily_summary", false)) }

    val reminderManager = remember { top.foxmoe.releasely.services.MedicationReminderManager(context) }

    fun save() {
        prefs.edit()
            .putBoolean("medication_reminder", medicationReminder)
            .putBoolean("cycle_reminder", cycleReminder)
            .putBoolean("daily_summary", dailySummary)
            .apply()
    }

    fun updateMedicationReminders(enabled: Boolean) {
        if (enabled) {
            kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                reminderManager.rescheduleAll()
            }
        } else {
            reminderManager.cancelAll()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "返回")
            }
            Text(text = "通知设置", fontSize = 24.sp, fontWeight = FontWeight.Medium)
        }

        Spacer(modifier = Modifier.height(24.dp))

        NotificationSwitchItem(
            title = "药物提醒",
            subtitle = "按时提醒服用药物",
            checked = medicationReminder,
            onCheckedChange = {
                medicationReminder = it
                save()
                updateMedicationReminders(it)
            }
        )

        NotificationSwitchItem(
            title = "周期提醒",
            subtitle = "月经/排卵期提醒",
            checked = cycleReminder,
            onCheckedChange = {
                cycleReminder = it
                save()
            }
        )

        NotificationSwitchItem(
            title = "每日汇总",
            subtitle = "每日推送健康数据摘要",
            checked = dailySummary,
            onCheckedChange = {
                dailySummary = it
                save()
            }
        )
    }
}

@Composable
fun NotificationSwitchItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                Text(text = subtitle, fontSize = 12.sp, color = Color.Gray)
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}
