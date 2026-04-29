package top.foxmoe.releasely.services

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import kotlinx.coroutines.runBlocking
import top.foxmoe.releasely.database.AppDatabase
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

class MedicationReminderManager(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    companion object {
        fun getMedicationService(context: Context): MedicationService? {
            return try {
                val driver = AndroidSqliteDriver(AppDatabase.Schema, context, "releasely.db")
                val database = AppDatabase(driver)
                MedicationService(database)
            } catch (e: Exception) {
                null
            }
        }
    }

    /**
     * 为单个药物注册每日提醒。reminderTime 为 Unix 秒级时间戳（当天的时间）。
     */
    fun scheduleReminder(medicationId: String, name: String, dosage: String, reminderTime: Long) {
        if (isReminderDisabled()) return
        if (!canScheduleExactAlarms()) return

        val intent = Intent(context, MedicationReminderReceiver::class.java).apply {
            putExtra(MedicationReminderReceiver.EXTRA_MEDICATION_ID, medicationId)
            putExtra(MedicationReminderReceiver.EXTRA_MEDICATION_NAME, name)
            putExtra(MedicationReminderReceiver.EXTRA_MEDICATION_DOSAGE, dosage)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            medicationId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = calculateNextTriggerTime(reminderTime)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        }
    }

    /**
     * 取消单个药物的提醒
     */
    fun cancelReminder(medicationId: String) {
        val intent = Intent(context, MedicationReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            medicationId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    /**
     * 重新注册所有活跃药物的提醒（用于开机重启后或设置变更后）
     */
    suspend fun rescheduleAll() {
        if (isReminderDisabled()) {
            cancelAll()
            return
        }
        val medicationService = getMedicationService(context) ?: return
        val medications = medicationService.getActiveMedications()
        medications.forEach { med ->
            scheduleReminder(med.id, med.name, med.dosage, med.reminderTime)
        }
    }

    /**
     * 取消所有提醒
     */
    fun cancelAll() {
        val medicationService = getMedicationService(context) ?: return
        runBlocking {
            val medications = medicationService.getActiveMedications()
            medications.forEach { cancelReminder(it.id) }
        }
    }

    fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    fun requestExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    private fun isReminderDisabled(): Boolean {
        val prefs = context.getSharedPreferences("notifications", Context.MODE_PRIVATE)
        return !prefs.getBoolean("medication_reminder", true)
    }

    /**
     * 根据当天的 reminderTime 计算下一次触发的时间戳（毫秒）。
     * 如果今天时间已过，则顺延到明天同一时刻。
     */
    private fun calculateNextTriggerTime(reminderTime: Long): Long {
        val now = System.currentTimeMillis()
        val target = reminderTime * 1000L

        // reminderTime 只包含当天时分秒信息，需要映射到当前日期
        val targetCalendar = java.util.Calendar.getInstance().apply {
            timeInMillis = target
        }
        val nowCalendar = java.util.Calendar.getInstance().apply {
            timeInMillis = now
        }

        nowCalendar.set(java.util.Calendar.HOUR_OF_DAY, targetCalendar.get(java.util.Calendar.HOUR_OF_DAY))
        nowCalendar.set(java.util.Calendar.MINUTE, targetCalendar.get(java.util.Calendar.MINUTE))
        nowCalendar.set(java.util.Calendar.SECOND, 0)
        nowCalendar.set(java.util.Calendar.MILLISECOND, 0)

        if (nowCalendar.timeInMillis <= now) {
            nowCalendar.add(java.util.Calendar.DAY_OF_MONTH, 1)
        }

        return nowCalendar.timeInMillis
    }
}
