package com.example.bodifyaifitness.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Lên lịch lại alarm sau khi thiết bị khởi động lại.
 * AlarmManager bị xóa khi tắt nguồn, cần đăng ký lại.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            NotificationHelper.scheduleWorkoutReminder(context)
        }
    }
}
