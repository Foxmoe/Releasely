package top.foxmoe.releasely

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.core.context.GlobalContext
import top.foxmoe.releasely.shared.designsystem.components.ReleaselyTopAppBar
import top.foxmoe.releasely.shared.designsystem.theme.ReleaselyTheme
import top.foxmoe.releasely.shared.feature.dashboard.DashboardScreen
import top.foxmoe.releasely.shared.feature.panic.PanicOverlay
import top.foxmoe.releasely.shared.feature.record.RecordScreen
import top.foxmoe.releasely.shared.feature.settings.SettingsScreen
import top.foxmoe.releasely.shared.presentation.viewmodel.MainViewModel

private enum class RootTab { DASHBOARD, RECORD, SETTINGS }

@Composable
fun ReleaselyRootApp() {
    val viewModel = getViewModel<MainViewModel>()
    val uiState by viewModel.uiState.collectAsState()
    val snackbars = remember { SnackbarHostState() }
    var tab by remember { mutableStateOf(RootTab.DASHBOARD) }

    LaunchedEffect(uiState.message) {
        val message = uiState.message ?: return@LaunchedEffect
        snackbars.showSnackbar(message)
    }

    ReleaselyTheme(darkTheme = uiState.isDarkMode) {
        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                topBar = {
                    ReleaselyTopAppBar(
                        title = when (tab) {
                            RootTab.DASHBOARD -> "健康仪表盘"
                            RootTab.RECORD -> "记录"
                            RootTab.SETTINGS -> "设置"
                        },
                        onPanicClick = viewModel::togglePanicMode
                    )
                },
                snackbarHost = { SnackbarHost(hostState = snackbars) },
                bottomBar = {
                    NavigationBar {
                        NavigationBarItem(
                            selected = tab == RootTab.DASHBOARD,
                            onClick = { tab = RootTab.DASHBOARD },
                            label = { Text("Dashboard") },
                            icon = { Text("D") }
                        )
                        NavigationBarItem(
                            selected = tab == RootTab.RECORD,
                            onClick = { tab = RootTab.RECORD },
                            label = { Text("Record") },
                            icon = { Text("R") }
                        )
                        NavigationBarItem(
                            selected = tab == RootTab.SETTINGS,
                            onClick = { tab = RootTab.SETTINGS },
                            label = { Text("Settings") },
                            icon = { Text("S") }
                        )
                    }
                }
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    when (tab) {
                        RootTab.DASHBOARD -> DashboardScreen(
                            streakDays = uiState.streakDays,
                            weeklyTrend = uiState.weeklyTrend
                        )

                        RootTab.RECORD -> RecordScreen(
                            selectedType = uiState.selectedRecordType,
                            protectionEnabled = uiState.protectionEnabled,
                            pleasureLevel = uiState.pleasureLevel,
                            onTypeSelected = viewModel::onTypeSelected,
                            onProtectionChanged = viewModel::onProtectionChanged,
                            onPleasureChanged = viewModel::onPleasureChanged,
                            onSaveClick = viewModel::saveRecord
                        )

                        RootTab.SETTINGS -> SettingsScreen(
                            isCamouflageMode = uiState.isCamouflageMode,
                            isAppLockEnabled = uiState.isAppLockEnabled,
                            isNotificationEnabled = uiState.isNotificationEnabled,
                            isDarkMode = uiState.isDarkMode,
                            appVersion = "1.0.0",
                            onToggleCamouflage = viewModel::toggleCamouflage,
                            onToggleAppLock = viewModel::setAppLock,
                            onDestroyData = viewModel::clearAllData,
                            onToggleNotification = viewModel::setNotifications,
                            onToggleDarkMode = viewModel::setDarkMode,
                            onBackup = viewModel::backupData,
                            onRestore = viewModel::restoreData,
                            onPrivacyPolicyClick = viewModel::openPrivacyPolicy
                        )
                    }
                }
            }

            PanicOverlay(mode = uiState.panicMode)
        }
    }
}

@Composable
private inline fun <reified T : Any> getViewModel(): T {
    return remember { GlobalContext.get().get() }
}




