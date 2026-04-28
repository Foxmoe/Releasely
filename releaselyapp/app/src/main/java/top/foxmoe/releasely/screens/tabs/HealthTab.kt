package top.foxmoe.releasely.screens.tabs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ButtonDefaults
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import top.foxmoe.releasely.ReleaselyApp
import top.foxmoe.releasely.utils.formatTime

/**
 * 健康/用药 Tab：展示用药提醒列表，支持标记已服用和添加新药物
 * 点击服用后立即刷新 UI，避免延迟
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthTab(onAddClick: () -> Unit, onMarkTaken: (String) -> Unit) {
    val context = LocalContext.current
    val app = context.applicationContext as ReleaselyApp
    val scope = rememberCoroutineScope()

    var medications by remember { mutableStateOf<List<top.foxmoe.releasely.services.MedicationRecord>>(emptyList()) }

    LaunchedEffect(Unit) {
        medications = app.medicationService.getAllMedications()
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 用药提醒概览卡片
        item {
            HealthReminderCard(count = medications.count { it.lastTaken == null })
        }

        // 添加药物按钮
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
                        text = "添加药物",
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // 药物列表
        items(medications.size) { index ->
            val medication = medications[index]
            MedicationItem(
                name = medication.name,
                dosage = medication.dosage,
                nextTime = formatTime(medication.reminderTime),
                taken = medication.lastTaken != null,
                onTakenClick = {
                    scope.launch {
                        withContext(Dispatchers.IO) {
                            app.medicationService.markTaken(medication.id)
                        }
                        // 立即刷新列表，消除延迟感
                        medications = app.medicationService.getAllMedications()
                        onMarkTaken(medication.id)
                    }
                }
            )
        }
    }
}

/**
 * 用药提醒概览卡片，展示待提醒药物数量
 */
@Composable
private fun HealthReminderCard(count: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Email,
                    contentDescription = null,
                    tint = Color(0xFFFF9800),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "用药提醒",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                    Text(
                        text = "$count 个待提醒",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
            Icon(
                imageVector = Icons.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color.Gray
            )
        }
    }
}

/**
 * 单条药物记录卡片，展示药物名称、剂量、下次服用时间和服用状态
 */
@Composable
private fun MedicationItem(
    name: String,
    dosage: String,
    nextTime: String,
    taken: Boolean,
    onTakenClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (taken) Color(0xFFE8F5E9) else Color(0xFFFAFAFA)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
                Text(
                    text = dosage,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = nextTime,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = if (taken) "已服用" else "未服用",
                        fontSize = 11.sp,
                        color = if (taken) Color(0xFF4CAF50) else Color(0xFFE91E63)
                    )
                }
                if (!taken) {
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = onTakenClick,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("服用", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
