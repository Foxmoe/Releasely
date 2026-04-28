package top.foxmoe.releasely.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import top.foxmoe.releasely.ReleaselyApp
import top.foxmoe.releasely.screens.tabs.ActivityTab
import top.foxmoe.releasely.screens.tabs.CycleTab
import top.foxmoe.releasely.screens.tabs.HealthTab

/**
 * 数据记录页：包含行为、周期、健康三个子 Tab，支持添加各类记录
 */
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
        // 页面标题
        Text(
            text = "数据记录",
            fontSize = 28.sp,
            fontWeight = FontWeight.Light,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Tab 切换栏
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

        // 根据选中 Tab 展示对应内容
        when (selectedTab) {
            0 -> ActivityTab(
                onAddClick = { showActivityForm = true },
                onMarkTaken = { /* 行为记录无需标记服用 */ }
            )
            1 -> CycleTab(onAddClick = { showCycleForm = true })
            2 -> HealthTab(
                onAddClick = { showMedicationForm = true },
                onMarkTaken = { /* 服用逻辑已在 HealthTab 内处理 */ }
            )
        }
    }

    // 行为记录添加弹窗
    if (showActivityForm) {
        AlertDialog(
            onDismissRequest = { showActivityForm = false },
            confirmButton = {},
            text = {
                top.foxmoe.releasely.components.ActivityForm(
                    onSubmit = { date, type, protection, pleasure, mood, notes ->
                        GlobalScope.launch(Dispatchers.IO) {
                            app.activityService.insertActivity(
                                date, type, protection, pleasure, mood, notes, null
                            )
                        }
                        showActivityForm = false
                    },
                    onCancel = { showActivityForm = false }
                )
            }
        )
    }

    // 周期记录添加弹窗
    if (showCycleForm) {
        AlertDialog(
            onDismissRequest = { showCycleForm = false },
            confirmButton = {},
            text = {
                top.foxmoe.releasely.components.CycleForm(
                    onSubmit = { startDate, duration ->
                        GlobalScope.launch(Dispatchers.IO) {
                            app.cycleService.insertCycle(startDate, duration, null)
                        }
                        showCycleForm = false
                    },
                    onCancel = { showCycleForm = false }
                )
            }
        )
    }

    // 药物记录添加弹窗
    if (showMedicationForm) {
        AlertDialog(
            onDismissRequest = { showMedicationForm = false },
            confirmButton = {},
            text = {
                top.foxmoe.releasely.components.MedicationForm(
                    onSubmit = { name, dosage, reminderTime ->
                        GlobalScope.launch(Dispatchers.IO) {
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
