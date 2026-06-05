package com.example.bodifyaifitness.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.bodifyaifitness.dataclass.Schedule
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * BroadcastReceiver kích hoạt lúc 15:00 hàng ngày.
 * Quy trình:
 *  1. Kiểm tra user đã đăng nhập chưa
 *  2. Lấy active schedule từ Firestore
 *  3. Tìm WorkoutDay của hôm nay → lấy exerciseIds
 *  4. Lấy tên bài tập từ exercises_library
 *  5. Hiện notification
 *  6. Lên lịch alarm cho ngày mai
 */
class WorkoutReminderReceiver : BroadcastReceiver() {

    private val TAG = "WorkoutReminderReceiver"

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            try {
                handleReminder(context)
            } catch (e: Exception) {
                Log.e(TAG, "Lỗi xử lý reminder", e)
            } finally {
                // Lên lịch lại cho 15:00 ngày mai
                NotificationHelper.scheduleWorkoutReminder(context)
                pendingResult.finish()
            }
        }
    }

    private fun handleReminder(context: Context) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            Log.d(TAG, "User chưa đăng nhập, bỏ qua reminder")
            return
        }

        val db    = Firebase.firestore
        val today = normDate(System.currentTimeMillis())

        // ── 1. Lấy active schedule ────────────────────────────────────────────
        val schedulesSnap = Tasks.await(
            db.collection("users").document(uid)
                .collection("schedules")
                .whereEqualTo("isActive", true)
                .get(),
            8, TimeUnit.SECONDS
        )

        if (schedulesSnap.isEmpty) {
            Log.d(TAG, "Không có schedule đang active")
            return
        }

        val schedule = schedulesSnap.documents.firstOrNull()
            ?.toObject(Schedule::class.java)
            ?: return

        // ── 2. Tìm WorkoutDay của hôm nay ────────────────────────────────────
        val todayDay = schedule.days.firstOrNull { normDate(it.date) == today }
        if (todayDay == null || todayDay.exerciseIds.isEmpty()) {
            Log.d(TAG, "Hôm nay không có lịch tập")
            return
        }

        // ── 3. Lấy tên bài tập (Firestore giới hạn whereIn = 30 items) ───────
        val ids = todayDay.exerciseIds.take(30)
        val exercisesSnap = Tasks.await(
            db.collection("exercises_library")
                .whereIn(FieldPath.documentId(), ids)
                .get(),
            8, TimeUnit.SECONDS
        )

        val names = exercisesSnap.documents.mapNotNull { doc ->
            doc.getString("name")?.replaceFirstChar { it.uppercase() }
        }.sorted()

        if (names.isEmpty()) {
            Log.d(TAG, "Không lấy được tên bài tập")
            return
        }

        // ── 4. Hiện notification ──────────────────────────────────────────────
        Log.d(TAG, "Hiển thị notification: ${names.size} bài tập")
        NotificationHelper.showWorkoutNotification(context, names)
    }

    /** Normalize về 00:00:00 để so sánh ngày. */
    private fun normDate(ms: Long): Long =
        Calendar.getInstance().apply {
            timeInMillis = ms
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
}
