package com.example.bodifyaifitness.database

import android.util.Log
import com.example.bodifyaifitness.dataclass.Exercise
import com.example.bodifyaifitness.dataclass.User
import com.example.bodifyaifitness.dataclass.WorkOutLog
import com.google.firebase.Firebase
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
                val exerciseList = mutableListOf<Exercise>()
                for (document in result) {
                    val exercise = document.toObject<Exercise>()
                    exerciseList.add(exercise)
                }
                onSuccess(exerciseList)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Lỗi lấy thư viện bài tập", e)
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