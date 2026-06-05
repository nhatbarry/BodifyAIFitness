package com.example.bodifyaifitness.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.bodifyaifitness.R
import java.util.Calendar

object NotificationHelper {

    const val CHANNEL_ID         = "workout_reminder"
    const val NOTIFICATION_ID    = 1001
    const val ALARM_REQUEST_CODE = 2001

    // ── Channel ───────────────────────────────────────────────────────────────

    fun createNotificationChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Nhắc nhở tập luyện",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Thông báo lịch tập lúc 15:00 mỗi ngày"
        }
        context.getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }

    // ── Schedule alarm ────────────────────────────────────────────────────────

    /**
     * Lên lịch alarm lúc 15:00 hôm nay (nếu đã qua thì lên lịch ngày mai).
     * Sau khi alarm kích hoạt, [WorkoutReminderReceiver] sẽ tự lên lịch lại cho hôm sau.
     */
    fun scheduleWorkoutReminder(context: Context) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val pendingIntent = buildPendingIntent(context)

        val targetMillis = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 15)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            // Nếu 15:00 hôm nay đã qua → lên lịch cho ngày mai
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }.timeInMillis

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, targetMillis, pendingIntent
                )
            } else {
                // Fallback: inexact repeating nếu không có quyền exact alarm
                alarmManager.setInexactRepeating(
                    AlarmManager.RTC_WAKEUP,
                    targetMillis,
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
                )
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP, targetMillis, pendingIntent
            )
        }
    }

    /** Huỷ alarm (dùng khi user tắt notification trong settings). */
    fun cancelWorkoutReminder(context: Context) {
        context.getSystemService(AlarmManager::class.java)
            .cancel(buildPendingIntent(context))
    }

    // ── Show notification ─────────────────────────────────────────────────────

    fun showWorkoutNotification(context: Context, exerciseNames: List<String>) {
        val shortList  = exerciseNames.take(5)
        val bodyShort  = shortList.joinToString(" · ")
        val bodyLong   = shortList.joinToString("\n") { "• $it" }
        val extraCount = exerciseNames.size - shortList.size

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("💪 Hôm nay có ${exerciseNames.size} bài tập!")
            .setContentText(bodyShort)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(
                        buildString {
                            append(bodyLong)
                            if (extraCount > 0) append("\n… và $extraCount bài khác")
                        }
                    )
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        context.getSystemService(NotificationManager::class.java)
            .notify(NOTIFICATION_ID, notification)
    }

    // ── Private ───────────────────────────────────────────────────────────────

    private fun buildPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, WorkoutReminderReceiver::class.java)
        return PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
