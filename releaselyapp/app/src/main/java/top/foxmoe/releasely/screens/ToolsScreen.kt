package top.foxmoe.releasely.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import top.foxmoe.releasely.ReleaselyApp
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * 工具页：展示各类健康计算工具的入口卡片
 * 根据性别过滤工具列表（男性不显示月经周期相关工具）
 */
@Composable
fun ToolsScreen() {
    val context = LocalContext.current
    val app = context.applicationContext as ReleaselyApp
    val isFemale = remember { app.isFemale() }

    var selectedTool by remember { mutableStateOf<ToolItem?>(null) }

    val tools = remember(isFemale) {
        val allTools = mutableListOf<ToolItem>()
        if (isFemale) {
            allTools.add(ToolItem("月经周期计算", Icons.Filled.DateRange, Color(0xFFE91E63), ToolType.CycleCalculator))
            allTools.add(ToolItem("安全期计算", Icons.Filled.Lock, Color(0xFF9C27B0), ToolType.SafePeriodCalculator))
            allTools.add(ToolItem("排卵期计算", Icons.Filled.Favorite, Color(0xFFE91E63), ToolType.OvulationCalculator))
        }
        allTools.add(ToolItem("药物提醒", Icons.Filled.Notifications, Color(0xFFFF9800), ToolType.MedicationReminder))
        allTools.add(ToolItem("健康报告", Icons.Filled.Info, Color(0xFF4CAF50), ToolType.HealthReport))
        allTools.add(ToolItem("知识库", Icons.Filled.Search, Color(0xFF795548), ToolType.KnowledgeBase))
        allTools.add(ToolItem("数据导出", Icons.Filled.Send, Color(0xFF2196F3), ToolType.DataExport))
        allTools.toList()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "实用工具",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Light,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // 每行展示 2 个工具卡片
            items(tools.chunked(2)) { rowTools ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowTools.forEach { tool ->
                        ToolCard(
                            tool = tool,
                            modifier = Modifier.weight(1f),
                            onClick = { selectedTool = tool }
                        )
                    }
                    // 补齐空位，保证布局对齐
                    if (rowTools.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // 工具详情弹窗
        selectedTool?.let { tool ->
            ToolDetailDialog(
                tool = tool,
                onDismiss = { selectedTool = null }
            )
        }
    }
}

enum class ToolType {
    CycleCalculator,
    SafePeriodCalculator,
    OvulationCalculator,
    MedicationReminder,
    HealthReport,
    KnowledgeBase,
    DataExport
}

/**
 * 工具项数据类
 */
data class ToolItem(
    val title: String,
    val icon: ImageVector,
    val color: Color,
    val type: ToolType
)

/**
 * 工具卡片组件，带图标和标题
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolCard(
    tool: ToolItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.height(120.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = tool.color.copy(alpha = 0.1f)),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(tool.color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = tool.icon,
                    contentDescription = null,
                    tint = tool.color,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = tool.title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
        }
    }
}

@Composable
fun ToolDetailDialog(tool: ToolItem, onDismiss: () -> Unit) {
    when (tool.type) {
        ToolType.CycleCalculator -> CycleCalculatorDialog(onDismiss)
        ToolType.SafePeriodCalculator -> SafePeriodCalculatorDialog(onDismiss)
        ToolType.OvulationCalculator -> OvulationCalculatorDialog(onDismiss)
        ToolType.MedicationReminder -> MedicationReminderDialog(onDismiss)
        ToolType.HealthReport -> HealthReportDialog(onDismiss)
        ToolType.KnowledgeBase -> KnowledgeScreen(onBack = onDismiss)
        ToolType.DataExport -> DataExportDialog(onDismiss)
    }
}

@Composable
fun CycleCalculatorDialog(onDismiss: () -> Unit) {
    var lastPeriodDate by remember { mutableStateOf("") }
    var cycleLength by remember { mutableStateOf("28") }
    var result by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("月经周期计算") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = lastPeriodDate,
                    onValueChange = { lastPeriodDate = it },
                    label = { Text("末次月经开始日期 (yyyy-MM-dd)") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = cycleLength,
                    onValueChange = { cycleLength = it },
                    label = { Text("周期长度 (天)") },
                    singleLine = true
                )
                if (result.isNotEmpty()) {
                    Text(result, color = Color(0xFFE91E63), fontWeight = FontWeight.Medium)
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                result = try {
                    val date = LocalDate.parse(lastPeriodDate)
                    val length = cycleLength.toInt()
                    val nextPeriod = date.plusDays(length.toLong())
                    val ovulation = date.plusDays((length - 14).toLong())
                    "预计下次月经：$nextPeriod\n预计排卵日：$ovulation"
                } catch (e: Exception) {
                    "请输入正确的日期格式，如 2026-04-01"
                }
            }) {
                Text("计算")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("关闭") }
        }
    )
}

@Composable
fun SafePeriodCalculatorDialog(onDismiss: () -> Unit) {
    var lastPeriodDate by remember { mutableStateOf("") }
    var cycleLength by remember { mutableStateOf("28") }
    var result by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("安全期计算") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = lastPeriodDate,
                    onValueChange = { lastPeriodDate = it },
                    label = { Text("末次月经开始日期 (yyyy-MM-dd)") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = cycleLength,
                    onValueChange = { cycleLength = it },
                    label = { Text("周期长度 (天)") },
                    singleLine = true
                )
                if (result.isNotEmpty()) {
                    Text(result, color = Color(0xFF9C27B0), fontWeight = FontWeight.Medium)
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                result = try {
                    val date = LocalDate.parse(lastPeriodDate)
                    val length = cycleLength.toInt()
                    val ovulation = date.plusDays((length - 14).toLong())
                    val safeStart1 = date.plusDays(5)
                    val safeEnd1 = ovulation.minusDays(5)
                    val safeStart2 = ovulation.plusDays(4)
                    val safeEnd2 = date.plusDays(length.toLong())
                    "前安全期：$safeStart1 ~ $safeEnd1\n后安全期：$safeStart2 ~ $safeEnd2\n注意：安全期并非绝对安全！"
                } catch (e: Exception) {
                    "请输入正确的日期格式，如 2026-04-01"
                }
            }) {
                Text("计算")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("关闭") }
        }
    )
}

@Composable
fun OvulationCalculatorDialog(onDismiss: () -> Unit) {
    var lastPeriodDate by remember { mutableStateOf("") }
    var cycleLength by remember { mutableStateOf("28") }
    var result by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("排卵期计算") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = lastPeriodDate,
                    onValueChange = { lastPeriodDate = it },
                    label = { Text("末次月经开始日期 (yyyy-MM-dd)") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = cycleLength,
                    onValueChange = { cycleLength = it },
                    label = { Text("周期长度 (天)") },
                    singleLine = true
                )
                if (result.isNotEmpty()) {
                    Text(result, color = Color(0xFFE91E63), fontWeight = FontWeight.Medium)
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                result = try {
                    val date = LocalDate.parse(lastPeriodDate)
                    val length = cycleLength.toInt()
                    val ovulation = date.plusDays((length - 14).toLong())
                    val fertileStart = ovulation.minusDays(5)
                    val fertileEnd = ovulation.plusDays(1)
                    "排卵日：$ovulation\n易孕期：$fertileStart ~ $fertileEnd"
                } catch (e: Exception) {
                    "请输入正确的日期格式，如 2026-04-01"
                }
            }) {
                Text("计算")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("关闭") }
        }
    )
}

@Composable
fun MedicationReminderDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val app = context.applicationContext as ReleaselyApp
    var medications by remember { mutableStateOf<List<top.foxmoe.releasely.services.MedicationRecord>>(emptyList()) }

    LaunchedEffect(Unit) {
        medications = app.medicationService.getAllMedications()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("药物提醒") },
        text = {
            if (medications.isEmpty()) {
                Text("暂无药物提醒，请在数据记录页添加。")
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    medications.forEach { med ->
                        val timeStr = java.time.format.DateTimeFormatter.ofPattern("HH:mm")
                            .format(java.time.Instant.ofEpochSecond(med.reminderTime).atZone(java.time.ZoneId.systemDefault()))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(med.name, fontWeight = FontWeight.Medium)
                                Text("$timeStr · ${med.dosage}", fontSize = 12.sp, color = Color.Gray)
                            }
                            Text(
                                if (med.lastTaken != null) "已服用" else "待服用",
                                color = if (med.lastTaken != null) Color(0xFF4CAF50) else Color(0xFFFF9800),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("关闭") }
        }
    )
}

@Composable
fun HealthReportDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val app = context.applicationContext as ReleaselyApp
    val authPrefs = remember { context.getSharedPreferences("auth", android.content.Context.MODE_PRIVATE) }
    val userId = remember { authPrefs.getLong("userId", -1L).takeIf { it != -1L } }

    var reportText by remember { mutableStateOf("正在生成...") }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val backendReport = userId?.let { uid ->
            try {
                app.healthReportService.generateReport(uid, "weekly")
            } catch (e: Exception) {
                null
            }
        }

        reportText = if (backendReport != null) {
            backendReport
        } else {
            val activities = app.activityService.getAllActivities()
            val total = activities.size
            val protected = activities.count { it.protection }
            val rate = if (total > 0) (protected * 100 / total) else 0
            val recent = activities.take(5)
            buildString {
                appendLine("=== 健康报告（本地统计）===")
                appendLine("总记录数：$total")
                appendLine("保护措施率：$rate%")
                appendLine("")
                if (recent.isNotEmpty()) {
                    appendLine("最近记录：")
                    recent.forEach {
                        appendLine("· ${it.type} (${if (it.protection) "有保护" else "无保护"})")
                    }
                } else {
                    appendLine("暂无记录，开始记录您的健康数据吧！")
                }
            }
        }
        isLoading = false
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("健康报告") },
        text = {
            if (isLoading) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("正在生成...")
                }
            } else {
                Text(reportText)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("关闭") }
        }
    )
}

@Composable
fun DataExportDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val app = context.applicationContext as ReleaselyApp
    var status by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("数据备份") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("导出所有本地记录为 JSON 备份文件，或从备份文件恢复数据。")
                if (status.isNotEmpty()) {
                    Text(status, color = when {
                        status.startsWith("成功") -> Color(0xFF4CAF50)
                        status.startsWith("恢复") -> Color(0xFF4CAF50)
                        else -> Color.Red
                    })
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                GlobalScope.launch(Dispatchers.IO) {
                    try {
                        val activities = app.activityService.getAllActivities()
                        val json = buildString {
                            appendLine("[")
                            activities.forEachIndexed { index, a ->
                                appendLine("  {")
                                appendLine("    \"date\": ${a.date},")
                                appendLine("    \"type\": \"${a.type}\",")
                                appendLine("    \"protection\": ${a.protection}")
                                appendLine("  }${if (index < activities.size - 1) "," else ""}")
                            }
                            appendLine("]")
                        }
                        val file = java.io.File(context.getExternalFilesDir(null), "releasely_backup.json")
                        file.writeText(json)
                        status = "成功导出至：${file.absolutePath}"
                    } catch (e: Exception) {
                        status = "导出失败：${e.message}"
                    }
                }
            }) {
                Text("导出")
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = {
                    GlobalScope.launch(Dispatchers.IO) {
                        try {
                            val file = java.io.File(context.getExternalFilesDir(null), "releasely_backup.json")
                            if (!file.exists()) {
                                status = "恢复失败：未找到备份文件"
                                return@launch
                            }
                            val json = file.readText()
                            val regex = """\{\s*"date":\s*(\d+),\s*"type":\s*"([^"]+)",\s*"protection":\s*(true|false)\s*\}""".toRegex()
                            val matches = regex.findAll(json)
                            var count = 0
                            matches.forEach { match ->
                                val date = match.groupValues[1].toLong()
                                val type = match.groupValues[2]
                                val protection = match.groupValues[3].toBooleanStrictOrNull() ?: false
                                app.activityService.insertActivity(date, type, protection, null, null, null, null)
                                count++
                            }
                            status = "恢复成功：已恢复 $count 条记录"
                        } catch (e: Exception) {
                            status = "恢复失败：${e.message}"
                        }
                    }
                }) {
                    Text("恢复")
                }
                TextButton(onClick = onDismiss) { Text("关闭") }
            }
        }
    )
}
