package top.foxmoe.releasely.shared.presentation.viewmodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import top.foxmoe.releasely.shared.domain.model.RecordType
import top.foxmoe.releasely.shared.domain.repository.HealthRecordRepository
import top.foxmoe.releasely.shared.domain.service.CamouflageController
import top.foxmoe.releasely.shared.domain.usecase.LoginUseCase
import top.foxmoe.releasely.shared.domain.usecase.SaveRecordUseCase
import top.foxmoe.releasely.shared.feature.panic.PanicMode
import top.foxmoe.releasely.shared.presentation.state.MainUiState

class MainViewModel(
    private val loginUseCase: LoginUseCase,
    private val saveRecordUseCase: SaveRecordUseCase,
    private val healthRecordRepository: HealthRecordRepository,
    private val camouflageController: CamouflageController
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _uiState = MutableStateFlow(
        MainUiState(isCamouflageMode = camouflageController.isCamouflageEnabled())
    )
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        refreshDashboard()
    }

    fun login(username: String, password: String) {
        scope.launch {
            _uiState.update { it.copy(isLoading = true, message = null) }
            val result = loginUseCase(username, password)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    message = result.fold(
                        onSuccess = { payload -> "欢迎回来，${payload.profile.displayName}" },
                        onFailure = { error -> error.message ?: "登录失败" }
                    )
                )
            }
        }
    }

    fun onTypeSelected(type: RecordType) {
        _uiState.update { it.copy(selectedRecordType = type) }
    }

    fun onProtectionChanged(enabled: Boolean) {
        _uiState.update { it.copy(protectionEnabled = enabled) }
    }

    fun onPleasureChanged(value: Float) {
        _uiState.update { it.copy(pleasureLevel = value.coerceIn(0f, 10f)) }
    }

    fun saveRecord() {
        scope.launch {
            val current = _uiState.value
            _uiState.update { it.copy(isLoading = true, message = null) }
            val result = saveRecordUseCase(
                type = current.selectedRecordType,
                protectionEnabled = current.protectionEnabled,
                pleasureLevel = current.pleasureLevel.toInt()
            )
            _uiState.update {
                it.copy(
                    isLoading = false,
                    message = result.fold(
                        onSuccess = { "记录已保存" },
                        onFailure = { error -> error.message ?: "保存失败" }
                    )
                )
            }
            if (result.isSuccess) {
                refreshDashboard()
            }
        }
    }

    fun togglePanicMode() {
        _uiState.update {
            val next = when (it.panicMode) {
                PanicMode.NONE -> PanicMode.CALCULATOR
                PanicMode.CALCULATOR -> PanicMode.WEATHER
                PanicMode.WEATHER -> PanicMode.NONE
            }
            it.copy(panicMode = next)
        }
    }

    fun clearPanicMode() {
        _uiState.update { it.copy(panicMode = PanicMode.NONE) }
    }

    fun toggleCamouflage() {
        val next = !_uiState.value.isCamouflageMode
        camouflageController.setCamouflageEnabled(next)
        _uiState.update {
            it.copy(
                isCamouflageMode = next,
                message = if (next) "桌面图标已伪装为计算器" else "已恢复默认图标"
            )
        }
    }

    fun setAppLock(enabled: Boolean) {
        _uiState.update { it.copy(isAppLockEnabled = enabled) }
    }

    fun setNotifications(enabled: Boolean) {
        _uiState.update { it.copy(isNotificationEnabled = enabled) }
    }

    fun setDarkMode(enabled: Boolean) {
        _uiState.update { it.copy(isDarkMode = enabled) }
    }

    fun clearAllData() {
        scope.launch {
            _uiState.update { it.copy(isLoading = true, message = null) }
            val result = healthRecordRepository.clearAllRecords()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    streakDays = if (result.isSuccess) 0 else it.streakDays,
                    weeklyTrend = if (result.isSuccess) emptyList() else it.weeklyTrend,
                    message = result.fold(
                        onSuccess = { "本地数据已销毁" },
                        onFailure = { error -> error.message ?: "数据销毁失败" }
                    )
                )
            }
        }
    }

    fun backupData() {
        _uiState.update { it.copy(message = "备份功能已触发（可接入云端/本地导出）") }
    }

    fun restoreData() {
        _uiState.update { it.copy(message = "恢复功能已触发（可接入文件导入）") }
    }

    fun openPrivacyPolicy() {
        _uiState.update { it.copy(message = "隐私协议：请在应用内 WebView 或官网页面展示") }
    }

    fun refreshDashboard() {
        scope.launch {
            val streak = healthRecordRepository.getCurrentStreakDays()
            val trend = healthRecordRepository.getWeeklyTrend()
            _uiState.update { it.copy(streakDays = streak, weeklyTrend = trend) }
        }
    }
}
