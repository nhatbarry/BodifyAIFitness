package com.example.bodifyaifitness.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.bodifyaifitness.R
import com.example.bodifyaifitness.composable.ExerciseListSection
import com.example.bodifyaifitness.composable.FeaturedWorkout
import com.example.bodifyaifitness.composable.GreetingSection
import com.example.bodifyaifitness.composable.MuscleGroupChipSection
import com.example.bodifyaifitness.composable.SearchBarSection
import com.example.bodifyaifitness.ui.theme.GymSurfaceBg
import com.example.bodifyaifitness.viewmodel.ExerciseViewModel
import com.example.bodifyaifitness.viewmodel.ScheduleState
import com.example.bodifyaifitness.viewmodel.ScheduleViewModel
import com.example.bodifyaifitness.viewmodel.UserProfileState
import com.example.bodifyaifitness.viewmodel.UserViewModel
import java.util.Calendar

@Composable
fun ExplorerPage(
    modifier: Modifier = Modifier,
    navController: NavController,
    onNavigateToStart: () -> Unit = {},
    exerciseViewModel: ExerciseViewModel = viewModel(),
    scheduleViewModel: ScheduleViewModel = viewModel(),
    userViewModel: UserViewModel = viewModel()
) {
    val exerciseState    by exerciseViewModel.exerciseState.collectAsState()
    val searchQuery      by exerciseViewModel.searchQuery.collectAsState()
    val searchResults    by exerciseViewModel.searchResults.collectAsState()
    val selectedCategory by exerciseViewModel.selectedCategory.collectAsState()
    val scheduleState    by scheduleViewModel.scheduleState.collectAsState()
    val userState        by userViewModel.userState.observeAsState()

    LaunchedEffect(Unit) {
        userViewModel.loadUserProfile()
        scheduleViewModel.loadSchedules()
    }

    val userName = when (val s = userState) {
        is UserProfileState.Success -> s.user.name.ifBlank { stringResource(R.string.default_athlete) }
        else -> stringResource(R.string.default_athlete)
    }

    val activeSchedule = (scheduleState as? ScheduleState.Success)
        ?.schedules?.firstOrNull { it.isActive }

    val todayStart = normDateExplorer(System.currentTimeMillis())
    val todayExerciseCount = activeSchedule
        ?.days
        ?.filter { normDateExplorer(it.date) == todayStart }
        ?.sumOf { it.exerciseIds.size }
        ?: 0

    Box(
        modifier = modifier
            .background(GymSurfaceBg)
            .fillMaxSize()
    ) {
        Column {
            GreetingSection(name = userName)

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

            FeaturedWorkout(
                scheduleName = activeSchedule?.name,
                todayExerciseCount = todayExerciseCount,
                onStartClick = onNavigateToStart
            )

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

private fun normDateExplorer(ms: Long): Long {
    val cal = Calendar.getInstance()
    cal.timeInMillis = ms
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}
