package com.example.bodifyaifitness.database

import android.util.Log
import com.example.bodifyaifitness.dataclass.Exercise
import com.example.bodifyaifitness.dataclass.Schedule
import com.example.bodifyaifitness.dataclass.User
import com.example.bodifyaifitness.dataclass.WorkOutLog
import com.example.bodifyaifitness.dataclass.WorkoutDay
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject

class FirebaseManager {

    private val db = Firebase.firestore
    private val TAG = "FirebaseManager"

    fun saveUserProfile(user: User, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("users")
            .document(user.id)
            .set(user)
            .addOnSuccessListener {
                Log.d(TAG, "Lưu thông tin user thành công!")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Lỗi lưu user", e)
                onFailure(e)
            }
    }

    fun getUserProfile(userId: String, onSuccess: (User?) -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val user = document.toObject<User>()
                    onSuccess(user)
                } else {
                    onSuccess(null)
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Lỗi lấy thông tin user", e)
                onFailure(e)
            }
    }

    fun addExerciseToLibrary(exercise: Exercise) {
        db.collection("exercises_library")
            .document(exercise.id)
            .set(exercise)
            .addOnSuccessListener { Log.d(TAG, "Đã thêm bài tập: ${exercise.name}") }
            .addOnFailureListener { e -> Log.e(TAG, "Lỗi thêm bài tập", e) }
    }

    fun getAllExercises(onSuccess: (List<Exercise>) -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("exercises_library")
            .get()
            .addOnSuccessListener { result ->
                val exerciseList = result.mapNotNull { document ->
                    document.toObject<Exercise>()?.copy(id = document.id)
                }
                onSuccess(exerciseList)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Lỗi lấy thư viện bài tập", e)
                onFailure(e)
            }
    }

    fun getExerciseById(exerciseId: String, onSuccess: (Exercise?) -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("exercises_library")
            .document(exerciseId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val exercise = document.toObject<Exercise>()?.copy(id = document.id)
                    onSuccess(exercise)
                } else {
                    onSuccess(null)
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Lỗi lấy bài tập theo ID", e)
                onFailure(e)
            }
    }

    fun saveWorkoutLog(userId: String, log: WorkOutLog, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        // Nếu id trống, tự sinh một cái ID ngẫu nhiên từ Firestore
        val logId = log.id.ifEmpty { db.collection("users").document().id }
        val finalLog = log.copy(id = logId)

        db.collection("users")
            .document(userId)
            .collection("workout_history")
            .document(logId)
            .set(finalLog)
            .addOnSuccessListener {
                Log.d(TAG, "Đã lưu nhật ký buổi tập thành công!")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Lỗi lưu nhật ký buổi tập", e)
                onFailure(e)
            }
    }

    // ── Schedule ──────────────────────────────────────────────────────────────

    fun saveSchedule(userId: String, schedule: Schedule, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        val docId = schedule.id.ifEmpty { db.collection("users").document().id }
        val final = schedule.copy(id = docId, userId = userId)
        db.collection("users").document(userId)
            .collection("schedules").document(docId)
            .set(final)
            .addOnSuccessListener { onSuccess(docId) }
            .addOnFailureListener { e -> Log.e(TAG, "Lỗi lưu schedule", e); onFailure(e) }
    }

    fun getSchedules(userId: String, onSuccess: (List<Schedule>) -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("users").document(userId)
            .collection("schedules")
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                val list = result.mapNotNull { it.toObject<Schedule>() }
                onSuccess(list)
            }
            .addOnFailureListener { e -> Log.e(TAG, "Lỗi lấy schedules", e); onFailure(e) }
    }

    fun deleteSchedule(userId: String, scheduleId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("users").document(userId)
            .collection("schedules").document(scheduleId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> Log.e(TAG, "Lỗi xóa schedule", e); onFailure(e) }
    }

    fun getScheduleById(userId: String, scheduleId: String, onSuccess: (Schedule?) -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("users").document(userId)
            .collection("schedules").document(scheduleId)
            .get()
            .addOnSuccessListener { doc ->
                onSuccess(if (doc != null && doc.exists()) doc.toObject<Schedule>() else null)
            }
            .addOnFailureListener { e -> Log.e(TAG, "Lỗi lấy schedule", e); onFailure(e) }
    }

    fun updateScheduleDays(userId: String, scheduleId: String, days: List<WorkoutDay>, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("users").document(userId)
            .collection("schedules").document(scheduleId)
            .update("days", days)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> Log.e(TAG, "Lỗi cập nhật ngày tập", e); onFailure(e) }
    }

    fun setActiveSchedule(userId: String, activeScheduleId: String?, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val colRef = db.collection("users").document(userId).collection("schedules")
        colRef.get()
            .addOnSuccessListener { docs ->
                val batch = db.batch()
                docs.forEach { doc ->
                    batch.update(doc.reference, "isActive", doc.id == activeScheduleId)
                }
                batch.commit()
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { e -> Log.e(TAG, "Lỗi set active schedule", e); onFailure(e) }
            }
            .addOnFailureListener { e -> Log.e(TAG, "Lỗi lấy schedules để set active", e); onFailure(e) }
    }

    fun getExercisesByIds(ids: List<String>, onSuccess: (List<Exercise>) -> Unit, onFailure: (Exception) -> Unit) {
        if (ids.isEmpty()) { onSuccess(emptyList()); return }
        db.collection("exercises_library")
            .whereIn(FieldPath.documentId(), ids.take(30))
            .get()
            .addOnSuccessListener { result ->
                onSuccess(result.mapNotNull { it.toObject<Exercise>()?.copy(id = it.id) })
            }
            .addOnFailureListener { e -> Log.e(TAG, "Lỗi lấy exercises theo IDs", e); onFailure(e) }
    }

    fun getWorkoutLogByDate(userId: String, dateKey: String, onSuccess: (WorkOutLog?) -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("users").document(userId)
            .collection("workout_logs").document(dateKey)
            .get()
            .addOnSuccessListener { doc ->
                onSuccess(if (doc.exists()) doc.toObject<WorkOutLog>() else null)
            }
            .addOnFailureListener { e -> Log.e(TAG, "Lỗi lấy workout log", e); onFailure(e) }
    }

    fun upsertWorkoutLog(userId: String, dateKey: String, log: WorkOutLog, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("users").document(userId)
            .collection("workout_logs").document(dateKey)
            .set(log)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> Log.e(TAG, "Lỗi lưu workout log", e); onFailure(e) }
    }

    fun getAllWorkoutLogs(
        userId: String,
        onSuccess: (Map<String, WorkOutLog>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("users").document(userId)
            .collection("workout_logs")
            .get()
            .addOnSuccessListener { result ->
                val map = result.mapNotNull { doc ->
                    val log = doc.toObject<WorkOutLog>()
                    if (log != null) doc.id to log else null
                }.toMap()
                onSuccess(map)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Lỗi lấy tất cả workout logs", e)
                onFailure(e)
            }
    }

    fun getWorkoutHistory(userId: String, onSuccess: (List<WorkOutLog>) -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("users")
            .document(userId)
            .collection("workout_history")
            .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING) // Sắp xếp buổi mới nhất lên đầu
            .get()
            .addOnSuccessListener { result ->
                val historyList = mutableListOf<WorkOutLog>()
                for (document in result) {
                    val workoutLog = document.toObject<WorkOutLog>()
                    historyList.add(workoutLog)
                }
                onSuccess(historyList)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Lỗi lấy lịch sử tập", e)
                onFailure(e)
            }
    }
}