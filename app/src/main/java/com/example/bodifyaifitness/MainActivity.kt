package com.example.bodifyaifitness

import android.Manifest
import android.app.AlarmManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.bodifyaifitness.composable.AppNavigation
import com.example.bodifyaifitness.notification.NotificationHelper
import com.example.bodifyaifitness.ui.theme.BodifyAIFitnessTheme
import com.example.bodifyaifitness.viewmodel.AuthViewModel

class MainActivity : AppCompatActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    // Launcher xin quyền POST_NOTIFICATIONS (API 33+)
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) scheduleReminderIfPossible()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Tạo notification channel (phải gọi trước khi hiện bất kỳ notification nào)
        NotificationHelper.createNotificationChannel(this)

        // Xin quyền & lên lịch alarm
        requestNotificationPermissionAndSchedule()

        setContent {
            BodifyAIFitnessTheme {
                AppNavigation(Modifier.fillMaxSize(), authViewModel = authViewModel)
            }
        }
    }

    // ── Notification permission flow ──────────────────────────────────────────

    private fun requestNotificationPermissionAndSchedule() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // API 33+ cần xin quyền POST_NOTIFICATIONS
            when {
                ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Đã có quyền → lên lịch alarm
                    scheduleReminderIfPossible()
                }
                else -> {
                    // Xin quyền → callback trong launcher
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            // Dưới API 33 không cần xin → lên lịch thẳng
            scheduleReminderIfPossible()
        }
    }

    private fun scheduleReminderIfPossible() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(AlarmManager::class.java)
            if (!alarmManager.canScheduleExactAlarms()) {
                // Mở Settings để user cấp quyền SCHEDULE_EXACT_ALARM
                // (chỉ cần làm 1 lần, sau đó BootReceiver sẽ tự reschedule)
                val intent = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                startActivity(intent)
                return
            }
        }
        NotificationHelper.scheduleWorkoutReminder(this)
    }
}
