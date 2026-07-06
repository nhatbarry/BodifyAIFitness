package com.example.bodifyaifitness.viewmodel

import androidx.lifecycle.ViewModel
import com.example.bodifyaifitness.database.FirebaseManager
import com.example.bodifyaifitness.dataclass.Exercise
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class PoseCameraState {
    object Loading : PoseCameraState()
    data class Ready(val exercise: Exercise) : PoseCameraState()
    object Unsupported : PoseCameraState()
    data class Error(val message: String) : PoseCameraState()
}

class PoseCameraViewModel : ViewModel() {

    private val firebaseManager = FirebaseManager()

    private val _state = MutableStateFlow<PoseCameraState>(PoseCameraState.Loading)
    val state: StateFlow<PoseCameraState> = _state.asStateFlow()

    fun loadExercise(exerciseId: String) {
        _state.value = PoseCameraState.Loading
        firebaseManager.getExerciseById(
            exerciseId = exerciseId,
            onSuccess = { exercise ->
                _state.value = when {
                    exercise == null -> PoseCameraState.Error("Không tìm thấy bài tập")
                    !exercise.isAISupported || exercise.aiAlgorithm.isBlank() -> PoseCameraState.Unsupported
                    else -> PoseCameraState.Ready(exercise)
                }
            },
            onFailure = { e -> _state.value = PoseCameraState.Error(e.message ?: "Không tải được bài tập") }
        )
    }
}
