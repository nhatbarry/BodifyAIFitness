package com.example.bodifyaifitness.viewmodel

import androidx.lifecycle.ViewModel
import com.example.bodifyaifitness.database.FirebaseManager
import com.example.bodifyaifitness.dataclass.CompleteExercise
import com.example.bodifyaifitness.dataclass.Exercise
import com.example.bodifyaifitness.dataclass.ExerciseSet
import com.example.bodifyaifitness.dataclass.WorkOutLog
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class WorkoutLogViewModel : ViewModel() {

    private val firebaseManager = FirebaseManager()
    private val auth = FirebaseAuth.getInstance()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _todayExercises = MutableStateFlow<List<Exercise>>(emptyList())
    val todayExercises: StateFlow<List<Exercise>> = _todayExercises

    private val _workoutLog = MutableStateFlow<WorkOutLog?>(null)
    val workoutLog: StateFlow<WorkOutLog?> = _workoutLog

    // Log của ngày được chọn trong ScheduleDetailPage (có thể là quá khứ)
    private val _selectedDateLog = MutableStateFlow<WorkOutLog?>(null)
    val selectedDateLog: StateFlow<WorkOutLog?> = _selectedDateLog

    // Tất cả logs → dùng cho biểu đồ và thống kê tổng hợp
    private val _allLogsMap = MutableStateFlow<Map<String, WorkOutLog>>(emptyMap())
    val allLogsMap: StateFlow<Map<String, WorkOutLog>> = _allLogsMap

    val todayKey: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    init {
        loadTodayData()
    }

    fun loadTodayData() {
        val uid = auth.currentUser?.uid ?: return
        val today = normToday()
        _isLoading.value = true

        firebaseManager.getSchedules(uid,
            onSuccess = { schedules ->
                // Only use the single active schedule
                val activeSchedule = schedules.firstOrNull { it.isActive }
                val exerciseIds = activeSchedule
                    ?.days
                    ?.filter { normDate(it.date) == today }
                    ?.flatMap { it.exerciseIds }
                    ?.distinct()
                    ?: emptyList()

                if (exerciseIds.isEmpty()) {
                    _todayExercises.value = emptyList()
                    _isLoading.value = false
                    return@getSchedules
                }

                firebaseManager.getExercisesByIds(exerciseIds,
                    onSuccess = { exercises ->
                        _todayExercises.value = exercises
                        firebaseManager.getWorkoutLogByDate(uid, todayKey,
                            onSuccess = { log ->
                                _workoutLog.value = log
                                _isLoading.value = false
                            },
                            onFailure = { _isLoading.value = false }
                        )
                    },
                    onFailure = { _isLoading.value = false }
                )
            },
            onFailure = { _isLoading.value = false }
        )
    }

    fun saveSets(exerciseId: String, exerciseName: String, sets: List<ExerciseSet>) {
        val uid = auth.currentUser?.uid ?: return
        val currentLog = _workoutLog.value ?: WorkOutLog(date = normToday())

        val updatedExercises = currentLog.exercise.toMutableList()
        val idx = updatedExercises.indexOfFirst { it.exerciseId == exerciseId }
        val updated = CompleteExercise(exerciseId = exerciseId, exerciseName = exerciseName, sets = sets)

        if (idx >= 0) updatedExercises[idx] = updated else updatedExercises.add(updated)

        val updatedLog = currentLog.copy(exercise = updatedExercises)
        _workoutLog.value = updatedLog

        firebaseManager.upsertWorkoutLog(uid, todayKey, updatedLog, {}, {})
    }

    fun loadAllLogs() {
        val uid = auth.currentUser?.uid ?: return
        firebaseManager.getAllWorkoutLogs(
            userId    = uid,
            onSuccess = { map -> _allLogsMap.value = map },
            onFailure = { }
        )
    }

    fun loadLogForDate(dateKey: String) {
        val uid = auth.currentUser?.uid ?: run {
            _selectedDateLog.value = null
            return
        }
        firebaseManager.getWorkoutLogByDate(
            userId    = uid,
            dateKey   = dateKey,
            onSuccess = { log -> _selectedDateLog.value = log },
            onFailure = { _selectedDateLog.value = null }
        )
    }

    private fun normToday(): Long = normDate(System.currentTimeMillis())

    private fun normDate(ms: Long): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = ms
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}
