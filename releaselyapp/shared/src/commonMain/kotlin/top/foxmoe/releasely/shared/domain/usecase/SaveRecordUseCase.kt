package top.foxmoe.releasely.shared.domain.usecase

import kotlinx.datetime.Clock
import top.foxmoe.releasely.shared.domain.model.HealthRecord
import top.foxmoe.releasely.shared.domain.model.RecordType
import top.foxmoe.releasely.shared.domain.repository.HealthRecordRepository
import kotlin.random.Random

class SaveRecordUseCase(
    private val healthRecordRepository: HealthRecordRepository
) {
    suspend operator fun invoke(
        type: RecordType,
        protectionEnabled: Boolean,
        pleasureLevel: Int,
        timestampMillis: Long = Clock.System.now().toEpochMilliseconds()
    ): Result<Unit> {
        if (pleasureLevel !in 0..10) {
            return Result.failure(IllegalArgumentException("pleasureLevel must be in 0..10"))
        }

        val record = HealthRecord(
            id = buildRecordId(),
            type = type,
            timestampMillis = timestampMillis,
            protectionEnabled = protectionEnabled,
            pleasureLevel = pleasureLevel
        )
        return healthRecordRepository.save(record)
    }

    private fun buildRecordId(): String {
        val random = Random.nextInt(1000, 9999)
        return "rec-${Clock.System.now().toEpochMilliseconds()}-$random"
    }
}
