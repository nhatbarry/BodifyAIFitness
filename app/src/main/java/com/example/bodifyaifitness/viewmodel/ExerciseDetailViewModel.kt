package com.example.bodifyaifitness.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bodifyaifitness.database.ExerciseDbRepository
import com.example.bodifyaifitness.database.FirebaseManager
import com.example.bodifyaifitness.dataclass.Exercise
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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

    // GIF URL fetch từ ExerciseDB API (khi Firestore không có sẵn)
    private val _apiGifUrl = MutableStateFlow<String?>(null)
    val apiGifUrl: StateFlow<String?> = _apiGifUrl.asStateFlow()

    // Instructions fetch từ ExerciseDB API
    private val _apiInstructions = MutableStateFlow<List<String>>(emptyList())
    val apiInstructions: StateFlow<List<String>> = _apiInstructions.asStateFlow()

    fun loadExercise(exerciseId: String) {
        _state.value = ExerciseDetailState.Loading
        _apiGifUrl.value = null
        _apiInstructions.value = emptyList()

        firebaseManager.getExerciseById(
            exerciseId = exerciseId,
            onSuccess = { exercise ->
                if (exercise != null) {
                    _state.value = ExerciseDetailState.Success(exercise)
                    // Nếu Firestore không có gifUrl → fetch từ API
                    if (exercise.gifUrl.isEmpty()) {
                        fetchGifFromApi(exercise.searchName)
                    }
                } else {
                    _state.value = ExerciseDetailState.NotFound
                }
            },
            onFailure = { e ->
                _state.value = ExerciseDetailState.Error(e.message ?: "Không tải được bài tập")
            }
        )
    }

    private fun fetchGifFromApi(exerciseName: String) {
        viewModelScope.launch {
            val gifUrl = ExerciseDbRepository.getGifUrl(exerciseName)
            _apiGifUrl.value = gifUrl
        }
    }
}
