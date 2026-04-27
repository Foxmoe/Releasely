package top.foxmoe.releasely.shared.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import top.foxmoe.releasely.shared.designsystem.components.ReleaselyTopAppBar
import top.foxmoe.releasely.shared.designsystem.theme.ReleaselyTheme
import top.foxmoe.releasely.shared.feature.dashboard.DashboardScreen
import top.foxmoe.releasely.shared.feature.panic.PanicOverlay
import top.foxmoe.releasely.shared.feature.record.RecordScreen
import top.foxmoe.releasely.shared.presentation.viewmodel.MainViewModel

private enum class RootTab { DASHBOARD, RECORD }

@Composable
fun ReleaselyRootApp(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var tab by remember { mutableStateOf(RootTab.DASHBOARD) }

    ReleaselyTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                topBar = {
                    ReleaselyTopAppBar(
                        title = if (tab == RootTab.DASHBOARD) "健康概览" else "健康记录",
                        onPanicClick = viewModel::togglePanicMode
                    )
                },
                bottomBar = {
                    NavigationBar {
                        NavigationBarItem(
                            selected = tab == RootTab.DASHBOARD,
                            onClick = { tab = RootTab.DASHBOARD },
                            label = { Text("首页") },
                            icon = { Text("D") }
                        )
                        NavigationBarItem(
                            selected = tab == RootTab.RECORD,
                            onClick = { tab = RootTab.RECORD },
                            label = { Text("记录") },
                            icon = { Text("R") }
                        )
                    }
                }
            ) { innerPadding ->
                Column(modifier = Modifier.padding(innerPadding).padding(horizontal = 16.dp, vertical = 12.dp)) {
                    when (tab) {
                        RootTab.DASHBOARD -> DashboardScreen(
                            biologicalSex = uiState.biologicalSex,
                            streakDays = uiState.streakDays,
                            recentSexCount = uiState.recentSexCount,
                            cycleDay = uiState.cycleDay,
                            nextPeriodInDays = uiState.nextPeriodInDays,
                            weeklyTrend = uiState.weeklyTrend
                        )

                        RootTab.RECORD -> RecordScreen(
                            biologicalSex = uiState.biologicalSex,
                            selectedSexType = uiState.selectedSexRecordType,
                            protectionEnabled = uiState.protectionEnabled,
                            pleasureLevel = uiState.pleasureLevel,
                            periodFlowLevel = uiState.periodFlowLevel,
                            onSexTypeSelected = viewModel::onSexTypeSelected,
                            onProtectionChanged = viewModel::onProtectionChanged,
                            onPleasureChanged = viewModel::onPleasureChanged,
                            onPeriodFlowChanged = viewModel::onPeriodFlowChanged,
                            onSaveSexClick = viewModel::saveSexRecord,
                            onSavePeriodClick = viewModel::savePeriodRecord
                        )
                    }
                }
            }

            PanicOverlay(mode = uiState.panicMode)
        }
    }
}

