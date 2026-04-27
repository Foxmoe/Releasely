package top.foxmoe.releasely.shared.domain.model

enum class RecordType {
    SEX_SOLO,
    SEX_PARTNER,
    MENSTRUATION
}

enum class BiologicalSex {
    MALE,
    FEMALE
}

data class HealthRecord(
    val id: String,
    val type: RecordType,
    val timestampMillis: Long,
    val protectionEnabled: Boolean,
    val pleasureLevel: Int
)

data class DailyTrend(
    val dayLabel: String,
    val score: Int
)
