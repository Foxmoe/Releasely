package top.foxmoe.releasely.shared.presentation.viewmodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import top.foxmoe.releasely.shared.domain.model.BiologicalSex
import top.foxmoe.releasely.shared.domain.model.RecordType
import top.foxmoe.releasely.shared.domain.repository.HealthRecordRepository
import top.foxmoe.releasely.shared.domain.service.CamouflageController
import top.foxmoe.releasely.shared.domain.usecase.LoginUseCase
import top.foxmoe.releasely.shared.domain.usecase.SaveRecordUseCase
import top.foxmoe.releasely.shared.feature.panic.PanicMode
import top.foxmoe.releasely.shared.presentation.state.MainUiState
import top.foxmoe.releasely.shared.presentation.state.ProfileItem
import kotlin.random.Random

class MainViewModel(
    private val loginUseCase: LoginUseCase,
    private val saveRecordUseCase: SaveRecordUseCase,
    private val healthRecordRepository: HealthRecordRepository,
    private val camouflageController: CamouflageController
) {
    private val dayMillis = 24 * 60 * 60 * 1000L
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
                    isLoggedIn = result.isSuccess,
                    accountName = result.getOrNull()?.profile?.displayName,
                    message = result.fold(
                        onSuccess = { payload -> "欢迎回来，${payload.profile.displayName}" },
                        onFailure = { error -> error.message ?: "登录失败" }
                    )
                )
            }
        }
    }

    fun syncToCloud() {
        val current = _uiState.value
        if (!current.isLoggedIn) {
            _uiState.update { it.copy(message = "请先登录账号后再同步") }
            return
        }
        val recordCount = current.recentRecords.size
        _uiState.update { it.copy(message = "云端同步已触发，本次将同步 $recordCount 条本地记录") }
    }

    fun switchProfile(profileId: String) {
        _uiState.update { current ->
            val target = current.profiles.firstOrNull { it.id == profileId } ?: return@update current
            current.copy(
                activeProfileId = target.id,
                activeProfileName = target.name,
                activeAvatar = target.avatar,
                biologicalSex = target.sex,
                message = "已切换到 ${target.name}"
            )
        }
        refreshDashboard()
    }

    fun addProfile(name: String, sex: BiologicalSex) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) {
            _uiState.update { it.copy(message = "档案名不能为空") }
            return
        }

        val newProfile = ProfileItem(
            id = "p-${Clock.System.now().toEpochMilliseconds()}-${Random.nextInt(10, 99)}",
            name = trimmed,
            avatar = trimmed.take(1).uppercase(),
            sex = sex
        )

        _uiState.update { current ->
            current.copy(
                profiles = current.profiles + newProfile,
                activeProfileId = newProfile.id,
                activeProfileName = newProfile.name,
                activeAvatar = newProfile.avatar,
                biologicalSex = newProfile.sex,
                message = "新档案已创建"
            )
        }
        refreshDashboard()
    }

    fun setBiologicalSex(sex: BiologicalSex) {
        _uiState.update { current ->
            current.copy(
                biologicalSex = sex,
                profiles = current.profiles.map {
                    if (it.id == current.activeProfileId) it.copy(sex = sex) else it
                }
            )
        }
        refreshDashboard()
    }

    fun onSexTypeSelected(type: RecordType) {
        if (type == RecordType.MENSTRUATION) return
        _uiState.update { it.copy(selectedSexRecordType = type) }
    }

    fun onProtectionChanged(enabled: Boolean) {
        _uiState.update { it.copy(protectionEnabled = enabled) }
    }

    fun onPleasureChanged(value: Float) {
        _uiState.update { it.copy(pleasureLevel = value.coerceIn(0f, 10f)) }
    }

    fun onPeriodFlowChanged(value: Float) {
        _uiState.update { it.copy(periodFlowLevel = value.coerceIn(1f, 5f)) }
    }

    fun saveSexRecord() {
        scope.launch {
            val current = _uiState.value
            _uiState.update { it.copy(isLoading = true, message = null) }
            val result = saveRecordUseCase(
                type = current.selectedSexRecordType,
                protectionEnabled = current.protectionEnabled,
                pleasureLevel = current.pleasureLevel.toInt()
            )
            _uiState.update {
                it.copy(
                    isLoading = false,
                    message = result.fold(
                        onSuccess = { "性生活记录已保存" },
                        onFailure = { error -> error.message ?: "保存失败" }
                    )
                )
            }
            if (result.isSuccess) {
                refreshDashboard()
            }
        }
    }

    fun savePeriodRecord() {
        scope.launch {
            val current = _uiState.value
            _uiState.update { it.copy(isLoading = true, message = null) }
            val result = saveRecordUseCase(
                type = RecordType.MENSTRUATION,
                protectionEnabled = false,
                pleasureLevel = current.periodFlowLevel.toInt() * 2
            )
            _uiState.update {
                it.copy(
                    isLoading = false,
                    message = result.fold(
                        onSuccess = { "月经记录已保存" },
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

    fun openTool(toolName: String) {
        _uiState.update { it.copy(message = "已进入 $toolName（工具页可继续接入实际功能）") }
    }

    fun refreshDashboard() {
        scope.launch {
            val nowMillis = Clock.System.now().toEpochMilliseconds()
            val sevenDaysAgoMillis = nowMillis - (6 * dayMillis)
            val streak = healthRecordRepository.getCurrentStreakDays()
            val trend = healthRecordRepository.getWeeklyTrend()
            val recentRecords = healthRecordRepository.getRecent(limit = 60)
            val sexCount = recentRecords.count {
                it.type != RecordType.MENSTRUATION && it.timestampMillis >= sevenDaysAgoMillis
            }
            val lastPeriod = recentRecords
                .filter { it.type == RecordType.MENSTRUATION }
                .maxByOrNull { it.timestampMillis }

            _uiState.update { current ->
                val cycleInfo = if (current.biologicalSex == BiologicalSex.FEMALE && lastPeriod != null) {
                    val daysSince = ((nowMillis - lastPeriod.timestampMillis) / dayMillis).toInt().coerceAtLeast(0)
                    val cycleDay = (daysSince % current.cycleLengthDays) + 1
                    val nextPeriodInDays = (current.cycleLengthDays - (daysSince % current.cycleLengthDays))
                        .let { if (it == current.cycleLengthDays) 0 else it }
                    Pair(cycleDay, nextPeriodInDays)
                } else {
                    Pair(null, null)
                }

                current.copy(
                    streakDays = streak,
                    recentSexCount = sexCount,
                    cycleDay = cycleInfo.first,
                    nextPeriodInDays = cycleInfo.second,
                    weeklyTrend = trend,
                    recentRecords = recentRecords
                )
            }
        }
    }
}
