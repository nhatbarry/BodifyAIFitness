package com.example.bodifyaifitness.viewmodel

import androidx.lifecycle.ViewModel
import com.example.bodifyaifitness.database.FirebaseManager
import com.example.bodifyaifitness.dataclass.Schedule
import com.example.bodifyaifitness.dataclass.WorkoutDay
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ScheduleViewModel : ViewModel() {

    private val firebaseManager = FirebaseManager()
    private val auth = FirebaseAuth.getInstance()

    private val _scheduleState = MutableStateFlow<ScheduleState>(ScheduleState.Loading)
    val scheduleState: StateFlow<ScheduleState> = _scheduleState

    private val _selectedSchedule = MutableStateFlow<Schedule?>(null)
    val selectedSchedule: StateFlow<Schedule?> = _selectedSchedule

    fun loadSchedules() {
        val uid = auth.currentUser?.uid ?: return
        _scheduleState.value = ScheduleState.Loading
        firebaseManager.getSchedules(
            userId = uid,
            onSuccess = { _scheduleState.value = ScheduleState.Success(it) },
            onFailure = { _scheduleState.value = ScheduleState.Error(it.message ?: "Unknown error") }
        )
    }

    fun loadScheduleById(scheduleId: String) {
        val uid = auth.currentUser?.uid ?: return
        firebaseManager.getScheduleById(
            userId = uid,
            scheduleId = scheduleId,
            onSuccess = { _selectedSchedule.value = it },
            onFailure = { }
        )
    }

    fun createSchedule(name: String) {
        val uid = auth.currentUser?.uid ?: return
        firebaseManager.saveSchedule(
            userId = uid,
            schedule = Schedule(name = name),
            onSuccess = { newId ->
                // Auto-activate the newly created schedule
                firebaseManager.setActiveSchedule(
                    userId = uid,
                    activeScheduleId = newId,
                    onSuccess = { loadSchedules() },
                    onFailure = { loadSchedules() }
                )
            },
            onFailure = { _scheduleState.value = ScheduleState.Error(it.message ?: "Save failed") }
        )
    }

    fun deleteSchedule(scheduleId: String) {
        val uid = auth.currentUser?.uid ?: return
        firebaseManager.deleteSchedule(
            userId = uid,
            scheduleId = scheduleId,
            onSuccess = { loadSchedules() },
            onFailure = { _scheduleState.value = ScheduleState.Error(it.message ?: "Delete failed") }
        )
    }

    fun setSelectedSchedule(schedule: Schedule) {
        _selectedSchedule.value = schedule
    }

    fun toggleActive(schedule: Schedule) {
        val uid = auth.currentUser?.uid ?: return
        // If already active → deactivate all; if inactive → activate this one
        val newActiveId = if (schedule.isActive) null else schedule.id
        firebaseManager.setActiveSchedule(
            userId = uid,
            activeScheduleId = newActiveId,
            onSuccess = { loadSchedules() },
            onFailure = { }
        )
    }

    fun getActiveSchedule(): Schedule? {
        return (_scheduleState.value as? ScheduleState.Success)
            ?.schedules?.firstOrNull { it.isActive }
    }

    fun addWorkoutDay(scheduleId: String, newDay: WorkoutDay, onSuccess: () -> Unit = {}) {
        val uid = auth.currentUser?.uid ?: return
        val current = _selectedSchedule.value ?: return
        // Lọc bỏ ngày cũ có cùng date, thêm ngày mới, rồi xóa luôn các ngày có exerciseIds rỗng
        val updatedDays = (current.days.filter { it.date != newDay.date } + newDay)
            .filter { it.exerciseIds.isNotEmpty() }

        // Cập nhật state ngay (optimistic) để các thao tác liên tiếp (vd vuốt xóa nhiều bài
        // liên tục) đọc được state mới nhất thay vì phải chờ Firestore round-trip — tránh
        // race condition ghi đè lẫn nhau.
        _selectedSchedule.value = current.copy(days = updatedDays)

        firebaseManager.updateScheduleDays(
            userId = uid,
            scheduleId = scheduleId,
            days = updatedDays,
            onSuccess = { onSuccess() },
            onFailure = {
                _selectedSchedule.value = current // rollback
                _scheduleState.value = ScheduleState.Error(it.message ?: "Update failed")
            }
        )
    }

    fun removeWorkoutDay(scheduleId: String, date: Long, onSuccess: () -> Unit = {}) {
        val uid = auth.currentUser?.uid ?: return
        val current = _selectedSchedule.value ?: return
        val updatedDays = current.days.filter { it.date != date }

        _selectedSchedule.value = current.copy(days = updatedDays) // optimistic

        firebaseManager.updateScheduleDays(
            userId = uid,
            scheduleId = scheduleId,
            days = updatedDays,
            onSuccess = { onSuccess() },
            onFailure = {
                _selectedSchedule.value = current // rollback
                _scheduleState.value = ScheduleState.Error(it.message ?: "Update failed")
            }
        )
    }

    /**
     * Removes a single exercise from a day, computing the new exerciseIds list from the
     * *current* [_selectedSchedule] value read at call time — not from a list the caller
     * (e.g. a swipe-to-delete callback) captured earlier. Swiping several items back-to-back
     * calls this repeatedly before Compose necessarily recomposes in between; if each call
     * were to recompute "the new list" from a Composable-side snapshot, a fast second swipe
     * could still be looking at the pre-first-removal list and resurrect the first item when
     * it writes back. Doing the read-modify-write here, against the always-fresh StateFlow
     * value, keeps every call correct regardless of recomposition timing.
     */
    fun removeExerciseFromDay(scheduleId: String, date: Long, exerciseId: String, onSuccess: () -> Unit = {}) {
        val current = _selectedSchedule.value ?: return
        val day = current.days.find { it.date == date } ?: return
        val updatedIds = day.exerciseIds.filter { it != exerciseId }
        if (updatedIds.isEmpty()) {
            removeWorkoutDay(scheduleId, date, onSuccess)
        } else {
            addWorkoutDay(scheduleId, WorkoutDay(date = date, exerciseIds = updatedIds), onSuccess)
        }
    }
}

sealed class ScheduleState {
    object Loading : ScheduleState()
    data class Success(val schedules: List<Schedule>) : ScheduleState()
    data class Error(val message: String) : ScheduleState()
}
