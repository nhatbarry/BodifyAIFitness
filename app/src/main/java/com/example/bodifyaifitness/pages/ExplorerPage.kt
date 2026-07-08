package com.example.bodifyaifitness.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.bodifyaifitness.R
import com.example.bodifyaifitness.composable.AiCameraFilterChip
import com.example.bodifyaifitness.composable.ExerciseListSection
import com.example.bodifyaifitness.composable.FeaturedWorkout
import com.example.bodifyaifitness.composable.GreetingSection
import com.example.bodifyaifitness.composable.MuscleGroupChipSection
import com.example.bodifyaifitness.composable.SearchBarSection
import com.example.bodifyaifitness.dataclass.Exercise
import com.example.bodifyaifitness.ui.theme.GymOrange
import com.example.bodifyaifitness.ui.theme.GymSurfaceBg
import com.example.bodifyaifitness.ui.theme.TextMuted
import com.example.bodifyaifitness.ui.theme.TextWhite
import com.example.bodifyaifitness.viewmodel.ExerciseState
import com.example.bodifyaifitness.viewmodel.ExerciseViewModel
import com.example.bodifyaifitness.viewmodel.ScheduleState
import com.example.bodifyaifitness.viewmodel.ScheduleViewModel
import com.example.bodifyaifitness.viewmodel.UserProfileState
import com.example.bodifyaifitness.viewmodel.UserViewModel
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExplorerPage(
    modifier: Modifier = Modifier,
    navController: NavController,
    onNavigateToStart: () -> Unit = {},
    exerciseViewModel: ExerciseViewModel = viewModel(),
    scheduleViewModel: ScheduleViewModel = viewModel(),
    userViewModel: UserViewModel = viewModel()
) {
    val exerciseState      by exerciseViewModel.exerciseState.collectAsState()
    val searchQuery        by exerciseViewModel.searchQuery.collectAsState()
    val selectedCategories by exerciseViewModel.selectedCategories.collectAsState()
    val aiCameraOnly       by exerciseViewModel.aiCameraOnly.collectAsState()
    val scheduleState      by scheduleViewModel.scheduleState.collectAsState()
    val userState          by userViewModel.userState.observeAsState()

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
    val todayDay   = activeSchedule?.days?.firstOrNull { normDateExplorer(it.date) == todayStart }
    val todayExerciseIds = todayDay?.exerciseIds ?: emptyList()
    val todayExerciseCount = todayExerciseIds.size

    // Lấy Exercise objects từ ExerciseViewModel để hiển thị trong bottom sheet
    val allExercises: List<Exercise> = (exerciseState as? ExerciseState.Success)?.exercises ?: emptyList()
    val todayExercises = remember(allExercises, todayExerciseIds) {
        val idSet = todayExerciseIds.toSet()
        allExercises.filter { it.id in idSet }
    }

    // ── Bottom sheet state ────────────────────────────────────────────────────
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope      = rememberCoroutineScope()
    var showSheet  by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .background(GymSurfaceBg)
            .fillMaxSize()
    ) {
        Column {
            GreetingSection(
                name = userName,
                todayExerciseCount = todayExerciseCount,
                onBellClick = {
                    scope.launch {
                        showSheet = true
                        sheetState.show()
                    }
                }
            )

            SearchBarSection(
                query = searchQuery,
                selectedCategories = selectedCategories,
                onQueryChange = { exerciseViewModel.onSearchQueryChange(it) }
            )

            FeaturedWorkout(
                scheduleName = activeSchedule?.name,
                todayExerciseCount = todayExerciseCount,
                onStartClick = onNavigateToStart
            )

            Row {
                AiCameraFilterChip(
                    selected = aiCameraOnly,
                    onToggle = { exerciseViewModel.toggleAiCameraOnly() }
                )
                MuscleGroupChipSection(
                    selectedCategories = selectedCategories,
                    onChipToggled = { category ->
                        exerciseViewModel.toggleCategory(category)
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            ExerciseListSection(
                state = exerciseState,
                onExerciseClick = { exercise ->
                    navController.navigate("exercise_detail/${exercise.id}")
                }
            )
        }
    }

    // ── Bottom sheet hiển thị bài tập hôm nay ────────────────────────────────
    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState,
            containerColor = Color(0xFF12121F),
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(top = 12.dp, bottom = 8.dp)
                        .size(width = 40.dp, height = 4.dp)
                        .background(Color(0xFF3A3A50), RoundedCornerShape(2.dp))
                )
            }
        ) {
            TodayWorkoutSheet(
                exercises = todayExercises,
                onGoToWorkout = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion { showSheet = false }
                    onNavigateToStart()
                }
            )
        }
    }
}

// ── Bottom sheet nội dung ─────────────────────────────────────────────────────

@Composable
private fun TodayWorkoutSheet(
    exercises: List<Exercise>,
    onGoToWorkout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        // Tiêu đề
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "💪 Lịch tập hôm nay",
                color = TextWhite,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = "${exercises.size} bài",
                color = GymOrange,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
        }

        Spacer(Modifier.height(4.dp))
        HorizontalDivider(color = Color(0xFF2A2A3E))
        Spacer(Modifier.height(8.dp))

        if (exercises.isEmpty()) {
            // Trường hợp không có lịch
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = TextMuted, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(12.dp))
                    Text(text = "Hôm nay không có lịch tập", color = TextMuted, fontSize = 14.sp)
                    Text(text = "Thêm bài tập vào giáo án để nhận nhắc nhở!", color = TextMuted, fontSize = 12.sp)
                }
            }
        } else {
            // Danh sách bài tập
            LazyColumn(modifier = Modifier.weight(1f, fill = false)) {
                itemsIndexed(exercises) { index, exercise ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp)
                    ) {
                        // Số thứ tự
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color(0xFF1E1E2E), RoundedCornerShape(8.dp))
                        ) {
                            Text(
                                text = "${index + 1}",
                                color = GymOrange,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = exercise.name,
                                color = TextWhite,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            )
                            if (exercise.muscleGroup.isNotBlank()) {
                                Text(
                                    text = exercise.muscleGroup,
                                    color = TextMuted,
                                    fontSize = 12.sp
                                )
                            }
                        }
                        if (exercise.isAISupported) {
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFF2A1A0A), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(text = "AI", color = GymOrange, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    if (index < exercises.lastIndex) {
                        HorizontalDivider(color = Color(0xFF1E1E2E))
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Nút bắt đầu tập
            Button(
                onClick = onGoToWorkout,
                colors = ButtonDefaults.buttonColors(containerColor = GymOrange),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(
                    text = "Bắt đầu tập ngay →",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }

        Spacer(Modifier.height(24.dp))
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


