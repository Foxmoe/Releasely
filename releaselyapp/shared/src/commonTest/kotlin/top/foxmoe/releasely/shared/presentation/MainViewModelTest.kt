package top.foxmoe.releasely.shared.presentation

import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import top.foxmoe.releasely.shared.domain.model.AuthToken
import top.foxmoe.releasely.shared.domain.model.DailyTrend
import top.foxmoe.releasely.shared.domain.model.HealthRecord
import top.foxmoe.releasely.shared.domain.model.LoginRequest
import top.foxmoe.releasely.shared.domain.model.LoginResult
import top.foxmoe.releasely.shared.domain.model.RecordType
import top.foxmoe.releasely.shared.domain.model.UserProfile
import top.foxmoe.releasely.shared.domain.repository.AuthRepository
import top.foxmoe.releasely.shared.domain.repository.HealthRecordRepository
import top.foxmoe.releasely.shared.domain.service.CamouflageController
import top.foxmoe.releasely.shared.domain.usecase.LoginUseCase
import top.foxmoe.releasely.shared.domain.usecase.SaveRecordUseCase
import top.foxmoe.releasely.shared.feature.panic.PanicMode
import top.foxmoe.releasely.shared.presentation.viewmodel.MainViewModel

class MainViewModelTest {

    @Test
    fun togglePanicModeCycles() = runTest {
        val repo = FakeHealthRepo()
        val viewModel = MainViewModel(
            loginUseCase = LoginUseCase(FakeAuthRepository()),
            saveRecordUseCase = SaveRecordUseCase(repo),
            healthRecordRepository = repo,
            camouflageController = FakeCamouflageController()
        )

        viewModel.togglePanicMode()
        viewModel.togglePanicMode()
        viewModel.togglePanicMode()

        delay(50)
        assertEquals(PanicMode.NONE, viewModel.uiState.value.panicMode)
    }

    @Test
    fun toggleCamouflageUpdatesState() = runTest {
        val repo = FakeHealthRepo()
        val viewModel = MainViewModel(
            loginUseCase = LoginUseCase(FakeAuthRepository()),
            saveRecordUseCase = SaveRecordUseCase(repo),
            healthRecordRepository = repo,
            camouflageController = FakeCamouflageController()
        )

        viewModel.toggleCamouflage()
        assertTrue(viewModel.uiState.value.isCamouflageMode)
    }
}

private class FakeAuthRepository : AuthRepository {
    override suspend fun login(request: LoginRequest): Result<LoginResult> {
        return Result.success(
            LoginResult(
                token = AuthToken("access", "refresh"),
                profile = UserProfile("u1", request.username, "Tester")
            )
        )
    }
}

private class FakeHealthRepo : HealthRecordRepository {
    val saved = mutableListOf<HealthRecord>()

    override suspend fun save(record: HealthRecord): Result<Unit> {
        saved += record
        return Result.success(Unit)
    }

    override suspend fun getRecent(limit: Long): List<HealthRecord> = saved

    override suspend fun getWeeklyTrend(): List<DailyTrend> =
        listOf(DailyTrend("4/1", 6), DailyTrend("4/2", 7))

    override suspend fun getCurrentStreakDays(): Int = 2

    override suspend fun clearAllRecords(): Result<Unit> {
        saved.clear()
        return Result.success(Unit)
    }
}

private class FakeCamouflageController : CamouflageController {
    private var enabled: Boolean = false

    override fun setCamouflageEnabled(enabled: Boolean) {
        this.enabled = enabled
    }

    override fun isCamouflageEnabled(): Boolean = enabled
}
