package com.example.bodifyaifitness.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import coil.imageLoader
import coil.request.ImageRequest
import com.example.bodifyaifitness.database.FirebaseManager
import com.example.bodifyaifitness.dataclass.Exercise
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ExerciseState {
    object Loading : ExerciseState()
    data class Success(val exercises: List<Exercise>) : ExerciseState()
    data class Error(val message: String) : ExerciseState()
}

class ExerciseViewModel(application: Application) : AndroidViewModel(application) {

    private val firebaseManager = FirebaseManager()

    private val _allExercises = MutableStateFlow<List<Exercise>>(emptyList())

    // Multi-select: rỗng = "All" (không lọc theo nhóm cơ nào)
    private val _selectedCategories = MutableStateFlow<Set<String>>(emptySet())
    val selectedCategories: StateFlow<Set<String>> = _selectedCategories.asStateFlow()

    private val _aiCameraOnly = MutableStateFlow(false)
    val aiCameraOnly: StateFlow<Boolean> = _aiCameraOnly.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _exerciseState = MutableStateFlow<ExerciseState>(ExerciseState.Loading)
    val exerciseState: StateFlow<ExerciseState> = _exerciseState.asStateFlow()

    init {
        fetchExercises()
    }

    private fun fetchExercises() {
        _exerciseState.value = ExerciseState.Loading
        firebaseManager.getAllExercises(
            onSuccess = { list ->
                _allExercises.value = list
                applyFilter()
                prefetchThumbnails(list)
            },
            onFailure = { e ->
                _exerciseState.value = ExerciseState.Error(e.message ?: "Không tải được bài tập")
            }
        )
    }

    private fun prefetchThumbnails(exercises: List<Exercise>) {
        val prefs = getApplication<Application>()
            .getSharedPreferences("bodify_prefs", Context.MODE_PRIVATE)
        if (prefs.getBoolean("thumbnails_prefetched", false)) return

        val context = getApplication<Application>()
        val imageLoader = context.imageLoader

        viewModelScope.launch {
            exercises.forEach { exercise ->
                if (exercise.thumbnailUrl.isNotEmpty()) {
                    imageLoader.enqueue(
                        ImageRequest.Builder(context)
                            .data(exercise.thumbnailUrl)
                            .build()
                    )
                }
            }
            prefs.edit().putBoolean("thumbnails_prefetched", true).apply()
        }
    }

    /** Bấm "All" -> bỏ hết lựa chọn; bấm 1 nhóm cơ -> bật/tắt nhóm đó (multi-select, OR). */
    fun toggleCategory(category: String) {
        _selectedCategories.value = if (category == "All") {
            emptySet()
        } else {
            val current = _selectedCategories.value
            if (category in current) current - category else current + category
        }
        applyFilter()
    }

    fun toggleAiCameraOnly() {
        _aiCameraOnly.value = !_aiCameraOnly.value
        applyFilter()
    }

    /** Gõ tên bài tập lọc trực tiếp danh sách chính, không còn dropdown gợi ý riêng. */
    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        applyFilter()
    }

    fun clearSearch() {
        _searchQuery.value = ""
        applyFilter()
    }

    /** Reset toàn bộ filter — dùng khi mở lại màn hình chọn bài tập (ExercisePickerScreen). */
    fun resetFilters() {
        _selectedCategories.value = emptySet()
        _aiCameraOnly.value = false
        _searchQuery.value = ""
        applyFilter()
    }

    private fun applyFilter() {
        val categories = _selectedCategories.value
        val query = _searchQuery.value.trim()

        var filtered = _allExercises.value
        if (categories.isNotEmpty()) {
            filtered = filtered.filter { ex -> categories.any { it.equals(ex.category, ignoreCase = true) } }
        }
        if (_aiCameraOnly.value) {
            filtered = filtered.filter { it.isAISupported }
        }
        if (query.isNotEmpty()) {
            filtered = filtered.filter { it.name.contains(query, ignoreCase = true) }
        }

        _exerciseState.value = ExerciseState.Success(filtered)
    }
}
