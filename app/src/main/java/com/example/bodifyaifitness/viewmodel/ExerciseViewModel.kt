package com.example.bodifyaifitness.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

class ExerciseViewModel : ViewModel() {

    private val firebaseManager = FirebaseManager()

    // Toàn bộ bài tập từ Firestore
    private val _allExercises = MutableStateFlow<List<Exercise>>(emptyList())

    // Category đang chọn ("All" = hiện tất cả)
    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    // Search
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Exercise>>(emptyList())
    val searchResults: StateFlow<List<Exercise>> = _searchResults.asStateFlow()

    // State hiển thị cho UI
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
            },
            onFailure = { e ->
                _exerciseState.value = ExerciseState.Error(e.message ?: "Không tải được bài tập")
            }
        )
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
        applyFilter()
        // Reset search khi đổi category
        _searchQuery.value = ""
        _searchResults.value = emptyList()
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }
        val category = _selectedCategory.value
        val pool = if (category == "All") {
            _allExercises.value
        } else {
            _allExercises.value.filter { it.category.equals(category, ignoreCase = true) }
        }
        _searchResults.value = pool
            .filter { it.name.contains(query.trim(), ignoreCase = true) }
            .take(8)
    }

    fun clearSearch() {
        _searchQuery.value = ""
        _searchResults.value = emptyList()
    }

    private fun applyFilter() {
        val cat = _selectedCategory.value
        val filtered = if (cat == "All") {
            _allExercises.value
        } else {
            _allExercises.value.filter {
                it.category.equals(cat, ignoreCase = true)
            }
        }
        _exerciseState.value = ExerciseState.Success(filtered)
    }
}
