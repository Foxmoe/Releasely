package top.foxmoe.releasely.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.runBlocking
import top.foxmoe.releasely.MainActivity
import top.foxmoe.releasely.R

class MedicationReminderReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "medication_reminder"
        const val EXTRA_MEDICATION_ID = "medication_id"
        const val EXTRA_MEDICATION_NAME = "medication_name"
        const val EXTRA_MEDICATION_DOSAGE = "medication_dosage"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val medId = intent.getStringExtra(EXTRA_MEDICATION_ID) ?: return
        val medName = intent.getStringExtra(EXTRA_MEDICATION_NAME) ?: "药物"
        val medDosage = intent.getStringExtra(EXTRA_MEDICATION_DOSAGE) ?: ""

        showNotification(context, medId, medName, medDosage)

        // 注册下一天的提醒
        val manager = MedicationReminderManager(context)
        val medicationService = MedicationReminderManager.getMedicationService(context)
        runBlocking {
            val medication = medicationService?.getMedicationById(medId)
            medication?.let {
                if (it.isActive) {
                    manager.scheduleReminder(it.id, it.name, it.dosage, it.reminderTime)
                }
            }
        }
    }

    private fun showNotification(context: Context, medId: String, name: String, dosage: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "药物提醒",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "按时提醒服用药物"
                setSound(
                    android.provider.Settings.System.DEFAULT_NOTIFICATION_URI,
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
            }
            notificationManager.createNotificationChannel(channel)
        }

        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, medId.hashCode(), contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("用药提醒")
            .setContentText("该服用 $name ${dosage.takeIf { it.isNotEmpty() } ?: ""} 了")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(medId.hashCode(), notification)
    }
}
