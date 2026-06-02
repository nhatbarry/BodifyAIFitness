package com.example.bodifyaifitness.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.bodifyaifitness.composable.ExerciseListSection
import com.example.bodifyaifitness.composable.FeaturedWorkout
import com.example.bodifyaifitness.composable.GreetingSection
import com.example.bodifyaifitness.composable.MuscleGroupChipSection
import com.example.bodifyaifitness.composable.SearchBarSection
import com.example.bodifyaifitness.ui.theme.GymSurfaceBg
import com.example.bodifyaifitness.viewmodel.ExerciseViewModel

@Composable
fun ExplorerPage(
    modifier: Modifier = Modifier,
    navController: NavController,
    exerciseViewModel: ExerciseViewModel = viewModel()
) {
    val exerciseState    by exerciseViewModel.exerciseState.collectAsState()
    val searchQuery      by exerciseViewModel.searchQuery.collectAsState()
    val searchResults    by exerciseViewModel.searchResults.collectAsState()
    val selectedCategory by exerciseViewModel.selectedCategory.collectAsState()

    Box(
        modifier = modifier
            .background(GymSurfaceBg)
            .fillMaxSize()
    ) {
        Column {
            GreetingSection()

            // ── Search bar (dropdown overlay dùng Popup) ─────────────────────
            SearchBarSection(
                query = searchQuery,
                results = searchResults,
                selectedCategory = selectedCategory,
                onQueryChange = { exerciseViewModel.onSearchQueryChange(it) },
                onResultClick = { exercise ->
                    exerciseViewModel.clearSearch()
                    navController.navigate("exercise_detail/${exercise.id}")
                }
            )

            FeaturedWorkout()

            MuscleGroupChipSection(
                onChipSelected = { category ->
                    exerciseViewModel.selectCategory(category)
                }
            )

            ExerciseListSection(
                state = exerciseState,
                onExerciseClick = { exercise ->
                    navController.navigate("exercise_detail/${exercise.id}")
                }
            )
        }
    }
}