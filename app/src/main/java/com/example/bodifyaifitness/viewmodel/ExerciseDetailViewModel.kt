package com.example.bodifyaifitness.viewmodel

import androidx.lifecycle.ViewModel
import com.example.bodifyaifitness.database.FirebaseManager
import com.example.bodifyaifitness.dataclass.Exercise
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class ExerciseDetailState {
    object Loading : ExerciseDetailState()
    object NotFound : ExerciseDetailState()
    data class Success(val exercise: Exercise) : ExerciseDetailState()
    data class Error(val message: String) : ExerciseDetailState()
}

class ExerciseDetailViewModel : ViewModel() {

    private val firebaseManager = FirebaseManager()

    private val _state = MutableStateFlow<ExerciseDetailState>(ExerciseDetailState.Loading)
    val state: StateFlow<ExerciseDetailState> = _state.asStateFlow()

    fun loadExercise(exerciseId: String) {
        _state.value = ExerciseDetailState.Loading
        firebaseManager.getExerciseById(
            exerciseId = exerciseId,
            onSuccess = { exercise ->
                if (exercise != null) {
                    _state.value = ExerciseDetailState.Success(exercise)
                } else {
                    _state.value = ExerciseDetailState.NotFound
                }
            },
            onFailure = { e ->
                _state.value = ExerciseDetailState.Error(e.message ?: "Không tải được bài tập")
            }
        )
    }
}
