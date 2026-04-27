package top.foxmoe.releasely

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.koin.core.context.GlobalContext
import top.foxmoe.releasely.shared.designsystem.theme.ReleaselyTheme
import top.foxmoe.releasely.shared.domain.model.BiologicalSex
import top.foxmoe.releasely.shared.domain.model.RecordType
import top.foxmoe.releasely.shared.feature.calendar.CalendarScreen
import top.foxmoe.releasely.shared.feature.my.MyScreen
import top.foxmoe.releasely.shared.feature.settings.SettingsScreen
import top.foxmoe.releasely.shared.feature.square.SquareScreen
import top.foxmoe.releasely.shared.presentation.state.ProfileItem
import top.foxmoe.releasely.shared.presentation.viewmodel.MainViewModel

private enum class RootTab { HOME, CALENDAR, SQUARE, ME }

@Composable
fun ReleaselyRootApp() {
    val viewModel = getViewModel<MainViewModel>()
    val uiState by viewModel.uiState.collectAsState()
    val snackbars = remember { SnackbarHostState() }
    val pagerState = rememberPagerState(initialPage = 0) { RootTab.entries.size }
    val scope = rememberCoroutineScope()
    var openSettingsInMe by remember { mutableStateOf(false) }
    var showSexCheckInDialog by remember { mutableStateOf(false) }
    var showPeriodCheckInDialog by remember { mutableStateOf(false) }
    var showProfileManagerDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.message) {
        val message = uiState.message ?: return@LaunchedEffect
        snackbars.showSnackbar(message)
    }

    ReleaselyTheme(darkTheme = uiState.isDarkMode) {
        if (uiState.profiles.isEmpty()) {
            WelcomeScreen { name, gender, _, _ ->
                val sex = if (gender == Gender.Ms) BiologicalSex.FEMALE else BiologicalSex.MALE
                viewModel.addProfile(name = name, sex = sex)
            }
            return@ReleaselyTheme
        }

        if (openSettingsInMe) {
            SettingsFullPage(
                uiState = uiState,
                onBack = { openSettingsInMe = false },
                onToggleAppLock = viewModel::setAppLock,
                onDestroyData = viewModel::clearAllData,
                onToggleNotification = viewModel::setNotifications,
                onToggleDarkMode = viewModel::setDarkMode,
                onSexChanged = viewModel::setBiologicalSex,
                onBackup = viewModel::backupData,
                onRestore = viewModel::restoreData,
                onPrivacyPolicyClick = viewModel::openPrivacyPolicy
            )
            return@ReleaselyTheme
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                if (pagerState.currentPage == RootTab.HOME.ordinal) {
                    ProfileSwitcherBar(
                        activeAvatar = uiState.activeAvatar,
                        activeProfileName = uiState.activeProfileName,
                        profiles = uiState.profiles,
                        onSwitchProfile = viewModel::switchProfile,
                        onAddProfile = viewModel::addProfile
                    )
                } else {
                    SimpleTopBar(
                        title = when (RootTab.entries[pagerState.currentPage]) {
                            RootTab.HOME -> "健康主页"
                            RootTab.CALENDAR -> "健康日历"
                            RootTab.SQUARE -> "健康广场"
                            RootTab.ME -> "我的账户"
                        },
                        showBack = false,
                        onBack = {}
                    )
                }
            },
            snackbarHost = { SnackbarHost(hostState = snackbars) },
            bottomBar = {
                NavigationBar {
                    RootTab.entries.forEachIndexed { index, item ->
                        val label = when (item) {
                            RootTab.HOME -> "首页"
                            RootTab.CALENDAR -> "日历"
                            RootTab.SQUARE -> "广场"
                            RootTab.ME -> "我的"
                        }
                        val icon = when (item) {
                            RootTab.HOME -> "H"
                            RootTab.CALENDAR -> "C"
                            RootTab.SQUARE -> "Q"
                            RootTab.ME -> "M"
                        }
                        NavigationBarItem(
                            selected = pagerState.currentPage == index,
                            onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                            label = { Text(label) },
                            icon = { Text(icon) }
                        )
                    }
                }
            }
        ) { innerPadding ->
            HorizontalPager(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                state = pagerState,
                userScrollEnabled = true
            ) { page ->
                when (RootTab.entries[page]) {
                    RootTab.HOME -> Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        HomeSquarePanels(
                            streakDays = uiState.streakDays,
                            weeklyTrend = uiState.weeklyTrend,
                            biologicalSex = uiState.biologicalSex,
                            onSexCheckIn = { showSexCheckInDialog = true },
                            onPeriodCheckIn = { showPeriodCheckInDialog = true }
                        )
                        HealthReminderCard(
                            cycleDay = uiState.cycleDay,
                            nextPeriodInDays = uiState.nextPeriodInDays,
                            recentSexCount = uiState.recentSexCount,
                            biologicalSex = uiState.biologicalSex
                        )
                    }

                    RootTab.CALENDAR -> Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        CalendarScreen(
                            records = uiState.recentRecords,
                            cycleDay = uiState.cycleDay,
                            nextPeriodInDays = uiState.nextPeriodInDays,
                            cycleLengthDays = uiState.cycleLengthDays,
                            streakDays = uiState.streakDays
                        )
                    }

                    RootTab.SQUARE -> Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        SquareScreen(onToolClick = viewModel::openTool)
                    }

                    RootTab.ME -> Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        MyScreen(
                            isLoggedIn = uiState.isLoggedIn,
                            accountName = uiState.accountName,
                            activeProfileName = uiState.activeProfileName,
                            activeAvatar = uiState.activeAvatar,
                            onLogin = viewModel::login,
                            onSyncNow = viewModel::syncToCloud,
                            onOpenProfileManager = { showProfileManagerDialog = true },
                            onOpenSettings = { openSettingsInMe = true }
                        )
                    }
                }
            }
        }

        if (showSexCheckInDialog) {
            AlertDialog(
                onDismissRequest = { showSexCheckInDialog = false },
                title = { Text("性生活打卡") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                modifier = Modifier.weight(1f),
                                onClick = { viewModel.onSexTypeSelected(RecordType.SEX_SOLO) }
                            ) {
                                Text(if (uiState.selectedSexRecordType == RecordType.SEX_SOLO) "个人 •" else "个人")
                            }
                            OutlinedButton(
                                modifier = Modifier.weight(1f),
                                onClick = { viewModel.onSexTypeSelected(RecordType.SEX_PARTNER) }
                            ) {
                                Text(if (uiState.selectedSexRecordType == RecordType.SEX_PARTNER) "伴侣 •" else "伴侣")
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("保护措施")
                            Switch(checked = uiState.protectionEnabled, onCheckedChange = viewModel::onProtectionChanged)
                        }
                        Text("评分 ${uiState.pleasureLevel.toInt()}/10")
                        Slider(
                            value = uiState.pleasureLevel,
                            onValueChange = viewModel::onPleasureChanged,
                            valueRange = 0f..10f,
                            steps = 9
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        viewModel.saveSexRecord()
                        showSexCheckInDialog = false
                    }) { Text("保存打卡") }
                },
                dismissButton = {
                    TextButton(onClick = { showSexCheckInDialog = false }) { Text("取消") }
                }
            )
        }

        if (showPeriodCheckInDialog && uiState.biologicalSex == BiologicalSex.FEMALE) {
            AlertDialog(
                onDismissRequest = { showPeriodCheckInDialog = false },
                title = { Text("月经打卡") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("流量强度 ${uiState.periodFlowLevel.toInt()}/5")
                        Slider(
                            value = uiState.periodFlowLevel,
                            onValueChange = viewModel::onPeriodFlowChanged,
                            valueRange = 1f..5f,
                            steps = 3
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        viewModel.savePeriodRecord()
                        showPeriodCheckInDialog = false
                    }) { Text("保存打卡") }
                },
                dismissButton = {
                    TextButton(onClick = { showPeriodCheckInDialog = false }) { Text("取消") }
                }
            )
        }

        if (showProfileManagerDialog) {
            AlertDialog(
                onDismissRequest = { showProfileManagerDialog = false },
                title = { Text("档案管理") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        uiState.profiles.forEach { profile ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.switchProfile(profile.id)
                                        showProfileManagerDialog = false
                                    }
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(profile.name)
                                if (profile.id == uiState.activeProfileId) Text("当前")
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        showProfileManagerDialog = false
                        scope.launch { pagerState.animateScrollToPage(RootTab.HOME.ordinal) }
                    }) { Text("完成") }
                }
            )
        }
    }
}

@Composable
private fun HomeSquarePanels(
    streakDays: Int,
    weeklyTrend: List<top.foxmoe.releasely.shared.domain.model.DailyTrend>,
    biologicalSex: BiologicalSex,
    onSexCheckIn: () -> Unit,
    onPeriodCheckIn: () -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier
                .weight(1f)
                .aspectRatio(1f)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(14.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("本周体验趋势", style = MaterialTheme.typography.bodyLarge)
                Text("连续打卡 $streakDays 天", style = MaterialTheme.typography.bodyMedium)
                MiniTrendChart(data = weeklyTrend, modifier = Modifier.fillMaxWidth().height(84.dp))
            }
        }

        Card(
            modifier = Modifier
                .weight(1f)
                .aspectRatio(1f)
                .clickable { onSexCheckIn() }
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(14.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text("打卡中心", style = MaterialTheme.typography.bodyLarge)
                Text("点此记录性生活", style = MaterialTheme.typography.bodyMedium)
                if (biologicalSex == BiologicalSex.FEMALE) {
                    TextButton(onClick = onPeriodCheckIn, modifier = Modifier.fillMaxWidth()) {
                        Text("月经打卡")
                    }
                }
            }
        }
    }
}

@Composable
private fun HealthReminderCard(
    cycleDay: Int?,
    nextPeriodInDays: Int?,
    recentSexCount: Int,
    biologicalSex: BiologicalSex
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("健康提醒", style = MaterialTheme.typography.bodyLarge)
            Text("近7天共记录 $recentSexCount 次", style = MaterialTheme.typography.bodyMedium)
            if (biologicalSex == BiologicalSex.FEMALE) {
                Text(
                    text = "当前周期：${cycleDay?.let { "第${it}天" } ?: "待记录"}，预计下次月经：${nextPeriodInDays?.let { "${it}天后" } ?: "待计算"}",
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                Text("建议保持稳定作息并按周复盘趋势", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun MiniTrendChart(
    data: List<top.foxmoe.releasely.shared.domain.model.DailyTrend>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("暂无数据", style = MaterialTheme.typography.bodyMedium)
        }
        return
    }

    val max = (data.maxOfOrNull { it.score } ?: 1).coerceAtLeast(1)
    Canvas(modifier = modifier) {
        val stepX = size.width / (data.size - 1).coerceAtLeast(1)
        val path = Path()
        data.forEachIndexed { index, point ->
            val x = index * stepX
            val y = size.height - (point.score / max.toFloat()) * size.height
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path = path, color = Color(25,25,25), style = Stroke(width = 4f))
        data.forEachIndexed { index, point ->
            val x = index * stepX
            val y = size.height - (point.score / max.toFloat()) * size.height
            drawCircle(color = Color(25,25,25), radius = 4f, center = Offset(x, y))
        }
    }
}

@Composable
private fun SettingsFullPage(
    uiState: top.foxmoe.releasely.shared.presentation.state.MainUiState,
    onBack: () -> Unit,
    onToggleAppLock: (Boolean) -> Unit,
    onDestroyData: () -> Unit,
    onToggleNotification: (Boolean) -> Unit,
    onToggleDarkMode: (Boolean) -> Unit,
    onSexChanged: (BiologicalSex) -> Unit,
    onBackup: () -> Unit,
    onRestore: () -> Unit,
    onPrivacyPolicyClick: () -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            SimpleTopBar(title = "系统设置", showBack = true, onBack = onBack)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            SettingsScreen(
                biologicalSex = uiState.biologicalSex,
                isAppLockEnabled = uiState.isAppLockEnabled,
                isNotificationEnabled = uiState.isNotificationEnabled,
                isDarkMode = uiState.isDarkMode,
                appVersion = "1.0.0",
                onToggleAppLock = onToggleAppLock,
                onDestroyData = onDestroyData,
                onToggleNotification = onToggleNotification,
                onToggleDarkMode = onToggleDarkMode,
                onSexChanged = onSexChanged,
                onBackup = onBackup,
                onRestore = onRestore,
                onPrivacyPolicyClick = onPrivacyPolicyClick
            )
        }
    }
}

@Composable
private fun SimpleTopBar(
    title: String,
    showBack: Boolean,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .statusBarsPadding()
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showBack) {
            TextButton(onClick = onBack) { Text("返回") }
        }
        Text(title, style = MaterialTheme.typography.displayLarge)
    }
}

@Composable
private fun ProfileSwitcherBar(
    activeAvatar: String,
    activeProfileName: String,
    profiles: List<ProfileItem>,
    onSwitchProfile: (String) -> Unit,
    onAddProfile: (String, BiologicalSex) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var openAdd by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var newSex by remember { mutableStateOf(BiologicalSex.FEMALE) }

    Row(
        modifier = Modifier
            .statusBarsPadding()
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(activeAvatar.ifEmpty { "?" }, color = MaterialTheme.colorScheme.onPrimary)
        }
        Row(
            modifier = Modifier
                .padding(start = 10.dp)
                .clickable { expanded = true },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(activeProfileName, style = MaterialTheme.typography.bodyLarge)
            Text("  v", style = MaterialTheme.typography.bodyMedium)
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            profiles.forEach { profile ->
                DropdownMenuItem(
                    text = { Text(profile.name) },
                    onClick = {
                        onSwitchProfile(profile.id)
                        expanded = false
                    }
                )
            }
            DropdownMenuItem(
                text = { Text("+ 新建档案") },
                onClick = {
                    expanded = false
                    openAdd = true
                }
            )
        }
    }

    if (openAdd) {
        AlertDialog(
            onDismissRequest = { openAdd = false },
            title = { Text("新建档案") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("档案名") },
                        singleLine = true
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = newSex == BiologicalSex.MALE, onClick = { newSex = BiologicalSex.MALE })
                        Text("男性", modifier = Modifier.width(56.dp))
                        RadioButton(selected = newSex == BiologicalSex.FEMALE, onClick = { newSex = BiologicalSex.FEMALE })
                        Text("女性")
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    onAddProfile(newName, newSex)
                    openAdd = false
                    newName = ""
                }) {
                    Text("创建")
                }
            },
            dismissButton = {
                TextButton(onClick = { openAdd = false }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private inline fun <reified T : Any> getViewModel(): T {
    return remember { GlobalContext.get().get() }
}



