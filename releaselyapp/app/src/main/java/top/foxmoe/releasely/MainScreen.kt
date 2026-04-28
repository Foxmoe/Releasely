package top.foxmoe.releasely

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Composable
fun MainScreen() {
    var selectedItem by remember { mutableIntStateOf(0) }
    val items = listOf("首页", "数据", "工具", "我的")
    val icons = listOf(
        Icons.Filled.Home,
        Icons.Filled.DateRange,
        Icons.Filled.Build,
        Icons.Filled.Person
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(icons[index], contentDescription = item) },
                        label = { Text(item) },
                        selected = selectedItem == index,
                        onClick = { selectedItem = index }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedItem) {
                0 -> HomeScreen()
                1 -> DataScreen()
                2 -> ToolsScreen()
                3 -> ProfileScreen()
            }
        }
    }
}

@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val app = context.applicationContext as ReleaselyApp

    var activityCount by remember { mutableIntStateOf(0) }
    var cycleDay by remember { mutableIntStateOf(0) }
    var cyclePhase by remember { mutableStateOf("") }
    var protectionRate by remember { mutableStateOf("0%") }

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

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "今日概览",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
        }

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
                TextButton(onClick = { }) {
                    Text("查看全部", color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun DataScreen() {
    val context = LocalContext.current
    val app = context.applicationContext as ReleaselyApp

    var selectedTab by remember { mutableIntStateOf(0) }
    var showActivityForm by remember { mutableStateOf(false) }
    var showCycleForm by remember { mutableStateOf(false) }
    var showMedicationForm by remember { mutableStateOf(false) }

    val tabs = listOf("行为", "周期", "健康")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "数据记录",
            fontSize = 28.sp,
            fontWeight = FontWeight.Light,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(16.dp))

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = Color.Black
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTab == index) FontWeight.Medium else FontWeight.Normal
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (selectedTab) {
            0 -> ActivityTab(
                onAddClick = { showActivityForm = true },
                onMarkTaken = { id -> /* handle */ }
            )
            1 -> CycleTab(onAddClick = { showCycleForm = true })
            2 -> HealthTab(
                onAddClick = { showMedicationForm = true },
                onMarkTaken = { id -> /* handle */ }
            )
        }
    }

    if (showActivityForm) {
        AlertDialog(
            onDismissRequest = { showActivityForm = false },
            confirmButton = {},
            text = {
                top.foxmoe.releasely.components.ActivityForm(
                    onSubmit = { date, type, protection, pleasure, mood, notes ->
                        kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                            app.activityService.insertActivity(date, type, protection, pleasure, mood, notes, null)
                        }
                        showActivityForm = false
                    },
                    onCancel = { showActivityForm = false }
                )
            }
        )
    }

    if (showCycleForm) {
        AlertDialog(
            onDismissRequest = { showCycleForm = false },
            confirmButton = {},
            text = {
                top.foxmoe.releasely.components.CycleForm(
                    onSubmit = { startDate, duration ->
                        kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                            app.cycleService.insertCycle(startDate, duration, null)
                        }
                        showCycleForm = false
                    },
                    onCancel = { showCycleForm = false }
                )
            }
        )
    }

    if (showMedicationForm) {
        AlertDialog(
            onDismissRequest = { showMedicationForm = false },
            confirmButton = {},
            text = {
                top.foxmoe.releasely.components.MedicationForm(
                    onSubmit = { name, dosage, reminderTime ->
                        kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                            app.medicationService.insertMedication(name, dosage, reminderTime)
                        }
                        showMedicationForm = false
                    },
                    onCancel = { showMedicationForm = false }
                )
            }
        )
    }
}

@Composable
fun ActivityTab(onAddClick: () -> Unit, onMarkTaken: (String) -> Unit) {
    val context = LocalContext.current
    val app = context.applicationContext as ReleaselyApp

    var activities by remember { mutableStateOf<List<top.foxmoe.releasely.services.ActivityRecord>>(emptyList()) }

    LaunchedEffect(Unit) {
        activities = app.activityService.getAllActivities()
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
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

@Composable
fun CycleTab(onAddClick: () -> Unit) {
    val context = LocalContext.current
    val app = context.applicationContext as ReleaselyApp

    var cycles by remember { mutableStateOf<List<top.foxmoe.releasely.services.CycleRecord>>(emptyList()) }

    LaunchedEffect(Unit) {
        cycles = app.cycleService.getAllCycles()
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            CycleOverviewCard()
        }

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

@Composable
fun HealthTab(onAddClick: () -> Unit, onMarkTaken: (String) -> Unit) {
    val context = LocalContext.current
    val app = context.applicationContext as ReleaselyApp

    var medications by remember { mutableStateOf<List<top.foxmoe.releasely.services.MedicationRecord>>(emptyList()) }

    LaunchedEffect(Unit) {
        medications = app.medicationService.getAllMedications()
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            HealthReminderCard(count = medications.size)
        }

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

        items(medications.size) { index ->
            val medication = medications[index]
            MedicationItem(
                name = medication.name,
                dosage = medication.dosage,
                nextTime = formatTime(medication.reminderTime),
                taken = medication.lastTaken != null,
                onTakenClick = {
                    kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                        app.medicationService.markTaken(medication.id)
                    }
                }
            )
        }
    }
}

fun formatDate(timestamp: Long): String {
    val instant = Instant.ofEpochSecond(timestamp)
    val localDate = instant.atZone(ZoneId.systemDefault()).toLocalDate()
    return localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
}

fun formatTime(timestamp: Long): String {
    val instant = Instant.ofEpochSecond(timestamp)
    val localDateTime = instant.atZone(ZoneId.systemDefault()).toLocalDateTime()
    return localDateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
}

@Composable
fun ToolsScreen() {
    val tools = remember {
        listOf(
            ToolItem("月经周期计算", Icons.Filled.DateRange, Color(0xFFE91E63)),
            ToolItem("安全期计算", Icons.Filled.Security, Color(0xFF9C27B0)),
            ToolItem("排卵期计算", Icons.Filled.Favorite, Color(0xFFE91E63)),
            ToolItem("药物提醒", Icons.Filled.Notifications, Color(0xFFFF9800)),
            ToolItem("健康报告", Icons.Filled.Assessment, Color(0xFF4CAF50)),
            ToolItem("数据导出", Icons.Filled.Download, Color(0xFF2196F3))
        )
    }

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

        items(tools.chunked(2)) { rowTools ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowTools.forEach { tool ->
                    ToolCard(
                        tool = tool,
                        modifier = Modifier.weight(1f),
                        onClick = { }
                    )
                }
                if (rowTools.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

data class ToolItem(
    val title: String,
    val icon: ImageVector,
    val color: Color
)

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
fun ProfileScreen() {
    val context = LocalContext.current
    var showPartnerScreen by remember { mutableStateOf(false) }
    var showSecurityScreen by remember { mutableStateOf(false) }

    val menuItems = remember {
        listOf(
            MenuItem("个人资料", Icons.Filled.Person, Color(0xFF2196F3)),
            MenuItem("伴侣管理", Icons.Filled.People, Color(0xFF9C27B0)),
            MenuItem("安全设置", Icons.Filled.Lock, Color(0xFFFF9800)),
            MenuItem("通知设置", Icons.Filled.Notifications, Color(0xFFE91E63)),
            MenuItem("隐私设置", Icons.Filled.Security, Color(0xFF607D8B)),
            MenuItem("关于", Icons.Filled.Info, Color(0xFF9E9E9E)),
            MenuItem("退出登录", Icons.Filled.ExitToApp, Color(0xFFF44336))
        )
    }

    if (showPartnerScreen) {
        val partners = remember { mutableStateOf<List<PartnerDisplayItem>>(emptyList()) }
        LaunchedEffect(Unit) {
            val app = context.applicationContext as ReleaselyApp
            partners.value = app.partnerService.getAllPartners().map {
                PartnerDisplayItem(it.id, it.name, it.inviteCode, it.status)
            }
        }
        top.foxmoe.releasely.screens.PartnerScreen(
            partners = partners.value,
            onAddPartner = { name ->
                kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                    val app = context.applicationContext as ReleaselyApp
                    app.partnerService.insertPartner(name)
                }
            },
            onInvitePartner = { code ->
                // Handle invite
            },
            onBack = { showPartnerScreen = false }
        )
        return
    }

    if (showSecurityScreen) {
        top.foxmoe.releasely.screens.SecuritySettingsScreen(
            onBack = { showSecurityScreen = false },
            onDecoyNavigate = { }
        )
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            ProfileHeader()
            Spacer(modifier = Modifier.height(16.dp))
        }

        items(menuItems.size) { index ->
            val menuItem = menuItems[index]
            MenuListItem(
                menuItem = menuItem,
                onClick = {
                    when (menuItem.title) {
                        "伴侣管理" -> showPartnerScreen = true
                        "安全设置" -> showSecurityScreen = true
                    }
                }
            )
        }
    }
}

@Composable
fun ProfileHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(Color(0xFFBBDEFB)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "用",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = "用户名",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "已记录 0 天",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}

data class MenuItem(
    val title: String,
    val icon: ImageVector,
    val color: Color
)

@Composable
fun MenuListItem(
    menuItem: MenuItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
        onClick = onClick
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
                    imageVector = menuItem.icon,
                    contentDescription = null,
                    tint = menuItem.color,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = menuItem.title,
                    fontSize = 14.sp,
                    color = Color.Black
                )
            }
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

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

@Composable
fun RecordItem(
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

@Composable
fun Chip(text: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFE0E0E0)
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            color = Color.Black,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun CycleOverviewCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "当前周期",
                fontSize = 12.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Day -",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row {
                Text(
                    text = "预计下次月经: ",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(
                    text = "-",
                    fontSize = 12.sp,
                    color = Color.Black
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { 0f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = Color(0xFF2196F3),
                trackColor = Color.White,
            )
        }
    }
}

@Composable
fun CycleRecordItem(
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

@Composable
fun HealthReminderCard(count: Int) {
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
                    imageVector = Icons.Filled.Notifications,
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
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = Color.Gray
            )
        }
    }
}

@Composable
fun MedicationItem(
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