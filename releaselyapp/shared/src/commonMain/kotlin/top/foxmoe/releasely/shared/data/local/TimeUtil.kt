package top.foxmoe.releasely.shared.data.local

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

internal fun epochSecToDayLabel(epochSec: Long): String {
    val date = Instant.fromEpochSeconds(epochSec).toLocalDateTime(TimeZone.currentSystemDefault()).date
    return "${date.monthNumber}/${date.dayOfMonth}"
}
