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

    fun addWorkoutDay(scheduleId: String, newDay: WorkoutDay) {
        val uid = auth.currentUser?.uid ?: return
        val current = _selectedSchedule.value ?: return
        val updatedDays = current.days.filter { it.date != newDay.date } + newDay
        firebaseManager.updateScheduleDays(
            userId = uid,
            scheduleId = scheduleId,
            days = updatedDays,
            onSuccess = { _selectedSchedule.value = current.copy(days = updatedDays) },
            onFailure = { _scheduleState.value = ScheduleState.Error(it.message ?: "Update failed") }
        )
    }

    fun removeWorkoutDay(scheduleId: String, date: Long) {
        val uid = auth.currentUser?.uid ?: return
        val current = _selectedSchedule.value ?: return
        val updatedDays = current.days.filter { it.date != date }
        firebaseManager.updateScheduleDays(
            userId = uid,
            scheduleId = scheduleId,
            days = updatedDays,
            onSuccess = { _selectedSchedule.value = current.copy(days = updatedDays) },
            onFailure = { _scheduleState.value = ScheduleState.Error(it.message ?: "Update failed") }
        )
    }
}

sealed class ScheduleState {
    object Loading : ScheduleState()
    data class Success(val schedules: List<Schedule>) : ScheduleState()
    data class Error(val message: String) : ScheduleState()
}
