package top.foxmoe.releasely.shared.domain

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue
import top.foxmoe.releasely.shared.domain.model.DailyTrend
import top.foxmoe.releasely.shared.domain.model.HealthRecord
import top.foxmoe.releasely.shared.domain.model.RecordType
import top.foxmoe.releasely.shared.domain.repository.HealthRecordRepository
import top.foxmoe.releasely.shared.domain.usecase.SaveRecordUseCase

class SaveRecordUseCaseTest {

    @Test
    fun rejectInvalidPleasureRange() = runTest {
        val useCase = SaveRecordUseCase(FakeHealthRecordRepository())
        val result = useCase(
            type = RecordType.SEX_PARTNER,
            protectionEnabled = true,
            pleasureLevel = 99
        )

        assertTrue(result.isFailure)
    }

    @Test
    fun saveRecordSuccess() = runTest {
        val repository = FakeHealthRecordRepository()
        val useCase = SaveRecordUseCase(repository)

        val result = useCase(
            type = RecordType.SEX_SOLO,
            protectionEnabled = false,
            pleasureLevel = 8
        )

        assertTrue(result.isSuccess)
        assertTrue(repository.saved.isNotEmpty())
    }
}

private class FakeHealthRecordRepository : HealthRecordRepository {
    val saved = mutableListOf<HealthRecord>()

    override suspend fun save(record: HealthRecord): Result<Unit> {
        saved += record
        return Result.success(Unit)
    }

    override suspend fun getRecent(limit: Long): List<HealthRecord> = saved

    override suspend fun getWeeklyTrend(): List<DailyTrend> = emptyList()

    override suspend fun getCurrentStreakDays(): Int = 0

    override suspend fun clearAllRecords(): Result<Unit> {
        saved.clear()
        return Result.success(Unit)
    }
}
