package top.foxmoe.releasely.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

@Composable
fun DataScreen() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val app = context.applicationContext as ReleaselyApp
    val isFemale = remember { app.isFemale() }

    var selectedTab by remember { androidx.compose.runtime.mutableIntStateOf(0) }
    var showActivityForm by remember { mutableStateOf(false) }
    var showCycleForm by remember { mutableStateOf(false) }
    var showMedicationForm by remember { mutableStateOf(false) }

    val tabs = if (isFemale) {
        listOf("行为", "周期", "健康")
    } else {
        listOf("行为", "健康")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "数据记录",
            fontSize = 28.sp,
            fontWeight = FontWeight.Light,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "记录你的健康生活",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(20.dp))

        // Tab 切换栏
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp),
            divider = {}
        ) {
            tabs.forEachIndexed { index, title ->
                val isSelected = selectedTab == index
                Tab(
                    selected = isSelected,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when {
            selectedTab == 0 -> ActivityTab(
                onAddClick = { showActivityForm = true }
            )
            isFemale && selectedTab == 1 -> CycleTab(onAddClick = { showCycleForm = true })
            (isFemale && selectedTab == 2) || (!isFemale && selectedTab == 1) -> HealthTab(
                onAddClick = { showMedicationForm = true },
                onMarkTaken = { }
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

    if (showCycleForm && isFemale) {
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

    if (showMedicationForm) {
        AlertDialog(
            onDismissRequest = { showMedicationForm = false },
            confirmButton = {},
            text = {
                top.foxmoe.releasely.components.MedicationForm(
                    onSubmit = { name, dosage, reminderTime ->
                        GlobalScope.launch(Dispatchers.IO) {
                            val id = app.medicationService.insertMedication(name, dosage, reminderTime)
                            app.reminderManager.scheduleReminder(id, name, dosage, reminderTime)
                        }
                        showMedicationForm = false
                    },
                    onCancel = { showMedicationForm = false }
                )
            }
        )
    }
}
