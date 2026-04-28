package top.foxmoe.releasely.utils

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * 将 Unix 时间戳（秒）格式化为 yyyy-MM-dd 日期字符串
 */
fun formatDate(timestamp: Long): String {
    val instant = Instant.ofEpochSecond(timestamp)
    val localDate = instant.atZone(ZoneId.systemDefault()).toLocalDate()
    return localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
}

/**
 * 将 Unix 时间戳（秒）格式化为 HH:mm 时间字符串
 */
fun formatTime(timestamp: Long): String {
    val instant = Instant.ofEpochSecond(timestamp)
    val localDateTime = instant.atZone(ZoneId.systemDefault()).toLocalDateTime()
    return localDateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
}
