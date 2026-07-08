package com.example.bodifyaifitness.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Lên lịch lại alarm sau khi thiết bị khởi động lại hoặc app được cập nhật.
 * AlarmManager bị xóa khi tắt nguồn, cần đăng ký lại.
 * Chỉ reschedule nếu user đã bật thông báo trong cài đặt.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            // Chỉ lên lịch lại nếu user đã bật thông báo
            if (NotificationPrefs.isEnabled(context)) {
                NotificationHelper.scheduleWorkoutReminder(context)
            }
        }
    }
}
