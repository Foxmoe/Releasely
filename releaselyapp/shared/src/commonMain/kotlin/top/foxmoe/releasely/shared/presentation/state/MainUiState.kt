package top.foxmoe.releasely.shared.presentation.state

import top.foxmoe.releasely.shared.domain.model.DailyTrend
import top.foxmoe.releasely.shared.domain.model.RecordType
import top.foxmoe.releasely.shared.feature.panic.PanicMode

data class MainUiState(
    val isLoading: Boolean = false,
    val streakDays: Int = 0,
    val weeklyTrend: List<DailyTrend> = emptyList(),
    val selectedRecordType: RecordType = RecordType.RELAX,
    val protectionEnabled: Boolean = true,
    val pleasureLevel: Float = 6f,
    val isCamouflageMode: Boolean = false,
    val isAppLockEnabled: Boolean = false,
    val isNotificationEnabled: Boolean = true,
    val isDarkMode: Boolean = false,
    val panicMode: PanicMode = PanicMode.NONE,
    val message: String? = null
)
