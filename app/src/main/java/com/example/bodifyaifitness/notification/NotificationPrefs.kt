package com.example.bodifyaifitness.notification

import android.content.Context

/**
 * Helper object để đọc/ghi cài đặt thông báo vào SharedPreferences.
 * Đây là nguồn dữ liệu duy nhất (Single Source of Truth) cho:
 *  - Bật/tắt thông báo hàng ngày
 *  - Giờ và phút gửi thông báo
 */
object NotificationPrefs {

    private const val PREFS_NAME   = "notification_prefs"
    private const val KEY_ENABLED  = "notification_enabled"
    private const val KEY_HOUR     = "notification_hour"
    private const val KEY_MINUTE   = "notification_minute"

    // Giá trị mặc định: BẬT lúc 8:00 sáng
    const val DEFAULT_HOUR   = 8
    const val DEFAULT_MINUTE = 0

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // ── Getters ──────────────────────────────────────────────────────────────

    fun isEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_ENABLED, true)   // mặc định BẬT

    fun getHour(context: Context): Int =
        prefs(context).getInt(KEY_HOUR, DEFAULT_HOUR)

    fun getMinute(context: Context): Int =
        prefs(context).getInt(KEY_MINUTE, DEFAULT_MINUTE)

    // ── Setters ──────────────────────────────────────────────────────────────

    fun setEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_ENABLED, enabled).apply()
    }

    fun setTime(context: Context, hour: Int, minute: Int) {
        prefs(context).edit()
            .putInt(KEY_HOUR, hour)
            .putInt(KEY_MINUTE, minute)
            .apply()
    }
}
