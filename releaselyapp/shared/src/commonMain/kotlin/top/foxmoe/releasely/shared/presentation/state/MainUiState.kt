package top.foxmoe.releasely.shared.presentation.state

import top.foxmoe.releasely.shared.domain.model.DailyTrend
import top.foxmoe.releasely.shared.domain.model.BiologicalSex
import top.foxmoe.releasely.shared.domain.model.HealthRecord
import top.foxmoe.releasely.shared.domain.model.RecordType
import top.foxmoe.releasely.shared.feature.panic.PanicMode

data class ProfileItem(
    val id: String,
    val name: String,
    val avatar: String,
    val sex: BiologicalSex
)

data class MainUiState(
    val isLoading: Boolean = false,
    val biologicalSex: BiologicalSex = BiologicalSex.FEMALE,
    val profiles: List<ProfileItem> = emptyList(),
    val activeProfileId: String = "",
    val activeProfileName: String = "",
    val activeAvatar: String = "",
    val streakDays: Int = 0,
    val recentSexCount: Int = 0,
    val cycleDay: Int? = null,
    val nextPeriodInDays: Int? = null,
    val weeklyTrend: List<DailyTrend> = emptyList(),
    val recentRecords: List<HealthRecord> = emptyList(),
    val selectedSexRecordType: RecordType = RecordType.SEX_PARTNER,
    val protectionEnabled: Boolean = true,
    val pleasureLevel: Float = 6f,
    val periodFlowLevel: Float = 3f,
    val cycleLengthDays: Int = 28,
    val isCamouflageMode: Boolean = false,
    val isAppLockEnabled: Boolean = false,
    val isNotificationEnabled: Boolean = true,
    val isDarkMode: Boolean = false,
    val isLoggedIn: Boolean = false,
    val accountName: String? = null,
    val panicMode: PanicMode = PanicMode.NONE,
    val message: String? = null
)
