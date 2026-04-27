package top.foxmoe.releasely.shared.feature.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import top.foxmoe.releasely.shared.domain.model.HealthRecord
import top.foxmoe.releasely.shared.domain.model.RecordType

private data class DayCell(
    val date: LocalDate,
    val inCurrentMonth: Boolean,
    val hasSexRecord: Boolean,
    val hasPeriodRecord: Boolean,
    val isPredictedPeriod: Boolean
)

private enum class CalendarMode { MONTH, WEEK }

@Composable
fun CalendarScreen(
    records: List<HealthRecord>,
    cycleDay: Int?,
    nextPeriodInDays: Int?,
    cycleLengthDays: Int,
    streakDays: Int,
    modifier: Modifier = Modifier
) {
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    var displayYear by remember { mutableStateOf(today.year) }
    var displayMonth by remember { mutableStateOf(today.monthNumber) }
    var mode by remember { mutableStateOf(CalendarMode.MONTH) }
    var selectedDate by remember { mutableStateOf(today) }

    val monthStart = LocalDate(displayYear, displayMonth, 1)
    val monthLength = getMonthLength(displayYear, displayMonth)
    val firstWeekday = monthStart.dayOfWeek.ordinal

    val sexRecordDays = records
        .filter { it.type != RecordType.MENSTRUATION }
        .map { millisToDate(it.timestampMillis) }
        .toSet()
    val periodDays = records
        .filter { it.type == RecordType.MENSTRUATION }
        .map { millisToDate(it.timestampMillis) }
        .toSet()

    val predictedDays = buildSet {
        val next = nextPeriodInDays ?: return@buildSet
        if (next < 0) return@buildSet
        val firstPredictedStart = LocalDate.fromEpochDays(today.toEpochDays() + next)
        repeat(5) { add(LocalDate.fromEpochDays(firstPredictedStart.toEpochDays() + it)) }
        val secondStart = LocalDate.fromEpochDays(firstPredictedStart.toEpochDays() + cycleLengthDays)
        repeat(5) { add(LocalDate.fromEpochDays(secondStart.toEpochDays() + it)) }
    }

    val days = buildList {
        repeat(firstWeekday) {
            val shadowDate = LocalDate.fromEpochDays(monthStart.toEpochDays() - (firstWeekday - it))
            add(
                DayCell(
                    date = shadowDate,
                    inCurrentMonth = false,
                    hasSexRecord = sexRecordDays.contains(shadowDate),
                    hasPeriodRecord = periodDays.contains(shadowDate),
                    isPredictedPeriod = predictedDays.contains(shadowDate)
                )
            )
        }
        (1..monthLength).forEach { day ->
            val date = LocalDate(displayYear, displayMonth, day)
            add(
                DayCell(
                    date = date,
                    inCurrentMonth = true,
                    hasSexRecord = sexRecordDays.contains(date),
                    hasPeriodRecord = periodDays.contains(date),
                    isPredictedPeriod = predictedDays.contains(date)
                )
            )
        }
        while (size % 7 != 0) {
            val lastDate = if (isEmpty()) monthStart else this.last().date
            val nextDate = LocalDate.fromEpochDays(lastDate.toEpochDays() + 1)
            add(
                DayCell(
                    date = nextDate,
                    inCurrentMonth = false,
                    hasSexRecord = sexRecordDays.contains(nextDate),
                    hasPeriodRecord = periodDays.contains(nextDate),
                    isPredictedPeriod = predictedDays.contains(nextDate)
                )
            )
        }
    }

    val selectedRecords = records.filter { millisToDate(it.timestampMillis) == selectedDate }
    val weekStart = LocalDate.fromEpochDays(selectedDate.toEpochDays() - selectedDate.dayOfWeek.ordinal)
    val weekCells = (0..6).map { offset ->
        val date = LocalDate.fromEpochDays(weekStart.toEpochDays() + offset)
        DayCell(
            date = date,
            inCurrentMonth = date.monthNumber == displayMonth,
            hasSexRecord = sexRecordDays.contains(date),
            hasPeriodRecord = periodDays.contains(date),
            isPredictedPeriod = predictedDays.contains(date)
        )
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("日历", style = MaterialTheme.typography.displayLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedButton(onClick = { mode = CalendarMode.MONTH }) { Text(if (mode == CalendarMode.MONTH) "月视图 •" else "月视图") }
                OutlinedButton(onClick = { mode = CalendarMode.WEEK }) { Text(if (mode == CalendarMode.WEEK) "周视图 •" else "周视图") }
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                modifier = Modifier.weight(1f),
                onClick = {
                    val current = displayYear * 12 + (displayMonth - 1) - 1
                    displayYear = current / 12
                    displayMonth = current % 12 + 1
                }
            ) { Text("上月") }
            Box(
                modifier = Modifier
                    .weight(1.2f)
                    .height(40.dp)
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("${displayYear}年 ${displayMonth}月", style = MaterialTheme.typography.bodyLarge)
            }
            OutlinedButton(
                modifier = Modifier.weight(1f),
                onClick = {
                    val current = displayYear * 12 + (displayMonth - 1) + 1
                    displayYear = current / 12
                    displayMonth = current % 12 + 1
                }
            ) { Text("下月") }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(10.dp))
                .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CalendarStat(title = "连续打卡", value = "$streakDays 天", modifier = Modifier.weight(1f))
            CalendarStat(title = "周期天数", value = cycleDay?.let { "第${it}天" } ?: "暂无", modifier = Modifier.weight(1f))
            CalendarStat(title = "下次月经", value = nextPeriodInDays?.let { "${it}天后" } ?: "待记录", modifier = Modifier.weight(1f))
        }

        val weekLabels = listOf("一", "二", "三", "四", "五", "六", "日")
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            weekLabels.forEach { label ->
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(text = label, style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        val rows = if (mode == CalendarMode.MONTH) days.chunked(7) else listOf(weekCells)
        rows.forEach { week ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                week.forEach { day ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .background(
                                color = when {
                                    day.date == selectedDate -> MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
                                    day.isPredictedPeriod -> Color(0xFFE53970).copy(alpha = 0.14f)
                                    day.inCurrentMonth -> MaterialTheme.colorScheme.surface
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                },
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable(enabled = day.inCurrentMonth) { selectedDate = day.date }
                            .padding(6.dp),
                        contentAlignment = Alignment.TopStart
                    ) {
                        if (day.inCurrentMonth) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(text = day.date.dayOfMonth.toString(), style = MaterialTheme.typography.bodyMedium)
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    if (day.hasSexRecord) {
                                        Dot(color = Color(0xFF7E57C2))
                                    }
                                    if (day.hasPeriodRecord) {
                                        Dot(color = Color(0xFFE53970))
                                    }
                                    if (day.isPredictedPeriod && !day.hasPeriodRecord) {
                                        Dot(color = Color(0xFFFFB3C7))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        StreakBar(recordDays = sexRecordDays + periodDays, today = today)

        Text(
            text = "${selectedDate.monthNumber}/${selectedDate.dayOfMonth} 记录 ${selectedRecords.size} 条",
            style = MaterialTheme.typography.bodyLarge
        )
        if (selectedRecords.isEmpty()) {
            Text("当天暂无记录", style = MaterialTheme.typography.bodyMedium)
        } else {
            selectedRecords.take(4).forEach {
                val label = when (it.type) {
                    RecordType.SEX_SOLO -> "个人性生活"
                    RecordType.SEX_PARTNER -> "伴侣性生活"
                    RecordType.MENSTRUATION -> "月经记录"
                }
                Text("- $label 评分${it.pleasureLevel}", style = MaterialTheme.typography.bodyMedium)
            }
        }

        if (nextPeriodInDays != null) {
            Text(
                text = "粉色淡点为预测经期窗口（本次+下次）",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun CalendarStat(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(title, style = MaterialTheme.typography.labelSmall)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun StreakBar(
    recordDays: Set<LocalDate>,
    today: LocalDate
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(10.dp))
            .padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        (13 downTo 0).forEach { delta ->
            val day = LocalDate.fromEpochDays(today.toEpochDays() - delta)
            val active = recordDays.contains(day)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(if (active) 20.dp else 12.dp)
                    .background(
                        if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(4.dp)
                    )
            )
        }
    }
}

@Composable
private fun Dot(color: Color) {
    Box(
        modifier = Modifier
            .size(7.dp)
            .background(color = color, shape = RoundedCornerShape(50))
    )
}

private fun millisToDate(timestampMillis: Long): LocalDate {
    return Instant.fromEpochMilliseconds(timestampMillis)
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .date
}

private fun getMonthLength(year: Int, month: Int): Int {
    return when (month) {
        1, 3, 5, 7, 8, 10, 12 -> 31
        4, 6, 9, 11 -> 30
        2 -> if (isLeapYear(year)) 29 else 28
        else -> 30
    }
}

private fun isLeapYear(year: Int): Boolean {
    return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
}


