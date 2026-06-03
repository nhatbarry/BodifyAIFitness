package com.example.bodifyaifitness.pages

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import com.example.bodifyaifitness.R
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.bodifyaifitness.composable.MuscleGroupChipSection
import com.example.bodifyaifitness.dataclass.WorkoutDay
import com.example.bodifyaifitness.ui.theme.GymOrange
import com.example.bodifyaifitness.ui.theme.GymSurfaceBg
import com.example.bodifyaifitness.ui.theme.TextMuted
import com.example.bodifyaifitness.ui.theme.TextWhite
import com.example.bodifyaifitness.viewmodel.ExerciseState
import com.example.bodifyaifitness.viewmodel.ExerciseViewModel
import com.example.bodifyaifitness.viewmodel.ScheduleViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleDetailPage(
    scheduleId: String,
    navController: NavController,
    scheduleViewModel: ScheduleViewModel = viewModel(),
    exerciseViewModel: ExerciseViewModel = viewModel()
) {
    val schedule by scheduleViewModel.selectedSchedule.collectAsState()
    var showExercisePicker by remember { mutableStateOf(false) }
    var isSpeedDialExpanded by remember { mutableStateOf(false) }
    var copiedExerciseIds by remember { mutableStateOf<List<String>?>(null) }

    val todayNorm = remember { normDate(System.currentTimeMillis()) }
    var selectedDate by remember { mutableStateOf(todayNorm) }

    val nowCal = Calendar.getInstance()
    var displayYear by remember { mutableStateOf(nowCal.get(Calendar.YEAR)) }
    var displayMonth by remember { mutableStateOf(nowCal.get(Calendar.MONTH)) }

    LaunchedEffect(scheduleId) {
        scheduleViewModel.loadScheduleById(scheduleId)
    }

    val markedDates = remember(schedule) {
        schedule?.days?.map { normDate(it.date) }?.toSet() ?: emptySet()
    }

    val selectedDayExerciseIds = remember(schedule, selectedDate) {
        schedule?.days?.find { normDate(it.date) == selectedDate }?.exerciseIds ?: emptyList()
    }

    Box(modifier = Modifier.fillMaxSize().background(GymSurfaceBg).statusBarsPadding()) {
        if (showExercisePicker) {
            ExercisePickerScreen(
                exerciseViewModel = exerciseViewModel,
                preSelectedIds = selectedDayExerciseIds,
                onConfirm = { ids ->
                    scheduleViewModel.addWorkoutDay(
                        scheduleId,
                        WorkoutDay(date = selectedDate, exerciseIds = ids)
                    )
                    showExercisePicker = false
                },
                onDismiss = { showExercisePicker = false }
            )
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                // ── Top bar ──────────────────────────────────────────────────
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(listOf(Color(0xFF1A1A2E), GymSurfaceBg))
                        )
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBackIosNew,
                            contentDescription = "Back",
                            tint = TextWhite
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = schedule?.name ?: "",
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }

                // ── Calendar ─────────────────────────────────────────────────
                CalendarSection(
                    displayYear = displayYear,
                    displayMonth = displayMonth,
                    selectedDateMillis = selectedDate,
                    todayMillis = todayNorm,
                    markedDates = markedDates,
                    onDateSelected = { selectedDate = it },
                    onPrevMonth = {
                        if (displayMonth == 0) { displayYear--; displayMonth = 11 }
                        else displayMonth--
                    },
                    onNextMonth = {
                        if (displayMonth == 11) { displayYear++; displayMonth = 0 }
                        else displayMonth++
                    }
                )

                HorizontalDivider(
                    color = Color(0xFF2A2A3E),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                // ── Exercises for selected day ────────────────────────────────
                WorkoutDaySection(
                    modifier = Modifier.weight(1f),
                    selectedDate = selectedDate,
                    exerciseIds = selectedDayExerciseIds,
                    exerciseState = exerciseViewModel.exerciseState.collectAsState().value,
                    onRemoveExercise = { exerciseId ->
                        val updated = selectedDayExerciseIds.filter { it != exerciseId }
                        scheduleViewModel.addWorkoutDay(
                            scheduleId,
                            WorkoutDay(date = selectedDate, exerciseIds = updated)
                        )
                    }
                )
            }

            SpeedDial(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp),
                isExpanded = isSpeedDialExpanded,
                canCopy = selectedDayExerciseIds.isNotEmpty(),
                canPaste = copiedExerciseIds != null,
                onToggle = { isSpeedDialExpanded = !isSpeedDialExpanded },
                onAdd = {
                    showExercisePicker = true
                    isSpeedDialExpanded = false
                },
                onCopy = {
                    copiedExerciseIds = selectedDayExerciseIds.toList()
                    isSpeedDialExpanded = false
                },
                onPaste = {
                    copiedExerciseIds?.let { ids ->
                        scheduleViewModel.addWorkoutDay(
                            scheduleId,
                            WorkoutDay(date = selectedDate, exerciseIds = ids)
                        )
                    }
                    isSpeedDialExpanded = false
                }
            )
        }
    }
}

// ── Calendar ──────────────────────────────────────────────────────────────────

@Composable
private fun CalendarSection(
    displayYear: Int,
    displayMonth: Int,
    selectedDateMillis: Long,
    todayMillis: Long,
    markedDates: Set<Long>,
    onDateSelected: (Long) -> Unit,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    val dayHeaders = stringArrayResource(R.array.calendar_day_headers).toList()

    val firstDayCal = Calendar.getInstance().apply {
        set(Calendar.YEAR, displayYear)
        set(Calendar.MONTH, displayMonth)
        set(Calendar.DAY_OF_MONTH, 1)
    }
    val daysInMonth = firstDayCal.getActualMaximum(Calendar.DAY_OF_MONTH)
    // Monday-first offset: Calendar.DAY_OF_WEEK 1=Sun..7=Sat → Mon offset=0
    val offset = (firstDayCal.get(Calendar.DAY_OF_WEEK) - 2 + 7) % 7

    val cells = buildList<Int?> {
        repeat(offset) { add(null) }
        for (day in 1..daysInMonth) add(day)
        while (size % 7 != 0) add(null)
    }

    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
        // Month / year navigation
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onPrevMonth) {
                Icon(Icons.Default.KeyboardArrowLeft, contentDescription = null, tint = TextWhite)
            }
            Text(
                text = stringResource(R.string.calendar_month_year, displayMonth + 1, displayYear),
                color = TextWhite,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onNextMonth) {
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = TextWhite)
            }
        }

        // Day-of-week header
        Row(modifier = Modifier.fillMaxWidth()) {
            dayHeaders.forEachIndexed { i, label ->
                Text(
                    text = label,
                    color = if (i >= 5) Color(0xFFE74C3C) else TextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 6.dp)
                )
            }
        }

        // Date grid
        cells.chunked(7).forEach { week ->
            Row(modifier = Modifier.fillMaxWidth()) {
                week.forEach { day ->
                    Box(
                        contentAlignment = Alignment.TopCenter,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (day != null) {
                            val cellMillis = Calendar.getInstance().apply {
                                set(Calendar.YEAR, displayYear)
                                set(Calendar.MONTH, displayMonth)
                                set(Calendar.DAY_OF_MONTH, day)
                                set(Calendar.HOUR_OF_DAY, 0)
                                set(Calendar.MINUTE, 0)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }.timeInMillis

                            val isToday = cellMillis == todayMillis
                            val isSelected = cellMillis == selectedDateMillis
                            val isMarked = cellMillis in markedDates

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onDateSelected(cellMillis) }
                                    .padding(vertical = 3.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                isToday -> GymOrange
                                                isSelected -> Color(0xFF2A2A3E)
                                                else -> Color.Transparent
                                            }
                                        )
                                ) {
                                    Text(
                                        text = day.toString(),
                                        color = TextWhite,
                                        fontSize = 13.sp,
                                        fontWeight = if (isToday || isSelected) FontWeight.Bold
                                                     else FontWeight.Normal
                                    )
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                if (isMarked) {
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .clip(CircleShape)
                                            .background(GymOrange)
                                    )
                                } else {
                                    Spacer(modifier = Modifier.height(5.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Workout Day Section ───────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorkoutDaySection(
    modifier: Modifier = Modifier,
    selectedDate: Long,
    exerciseIds: List<String>,
    exerciseState: ExerciseState,
    onRemoveExercise: (String) -> Unit
) {
    val dateLabel = remember(selectedDate) {
        SimpleDateFormat("EEEE, dd/MM/yyyy", Locale("vi")).format(Date(selectedDate))
            .replaceFirstChar { it.uppercase() }
    }

    val exercises = when (exerciseState) {
        is ExerciseState.Success -> exerciseState.exercises.filter { it.id in exerciseIds }
        else -> emptyList()
    }

    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.FitnessCenter,
                contentDescription = null,
                tint = GymOrange,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = dateLabel, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        if (exercises.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = stringResource(R.string.empty_workout_day), color = TextMuted, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.empty_workout_day_hint),
                        color = TextMuted.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(exercises, key = { it.id }) { exercise ->
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { value ->
                            if (value == SwipeToDismissBoxValue.EndToStart) {
                                onRemoveExercise(exercise.id); true
                            } else false
                        }
                    )
                    SwipeToDismissBox(
                        state = dismissState,
                        enableDismissFromStartToEnd = false,
                        backgroundContent = {
                            val color by animateColorAsState(
                                targetValue = if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart)
                                    Color(0xFFE74C3C) else Color.Transparent,
                                label = "swipe_bg"
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(color)
                                    .padding(end = 20.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White)
                            }
                        }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF12121F))
                                .padding(10.dp)
                        ) {
                            AsyncImage(
                                model = exercise.thumbnail.ifEmpty { null },
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF1A1A2E))
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = exercise.name.replaceFirstChar { it.uppercase() },
                                    color = TextWhite,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                                if (exercise.category.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = exercise.category.replaceFirstChar { it.uppercase() },
                                        color = TextMuted,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Exercise Picker ───────────────────────────────────────────────────────────

@Composable
private fun ExercisePickerScreen(
    exerciseViewModel: ExerciseViewModel,
    preSelectedIds: List<String>,
    onConfirm: (List<String>) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedIds by remember(preSelectedIds) { mutableStateOf(preSelectedIds.toSet()) }
    val exerciseState by exerciseViewModel.exerciseState.collectAsState()
    val searchQuery by exerciseViewModel.searchQuery.collectAsState()
    val searchResults by exerciseViewModel.searchResults.collectAsState()

    LaunchedEffect(Unit) { exerciseViewModel.selectCategory("All") }

    val displayList = if (searchQuery.isNotEmpty()) searchResults
                      else (exerciseState as? ExerciseState.Success)?.exercises ?: emptyList()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GymSurfaceBg)
    ) {
        // Top bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Color(0xFF1A1A2E), GymSurfaceBg)))
                .padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Đóng", tint = TextWhite)
            }
            Text(
                text = stringResource(R.string.title_pick_exercises),
                color = TextWhite,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = { onConfirm(selectedIds.toList()) }) {
                Text(
                    text = if (selectedIds.isEmpty()) stringResource(R.string.btn_done) else stringResource(R.string.btn_add_count, selectedIds.size),
                    color = GymOrange,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }

        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { exerciseViewModel.onSearchQueryChange(it) },
            placeholder = { Text(stringResource(R.string.placeholder_search_exercise), color = TextMuted) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted)
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { exerciseViewModel.clearSearch() }) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = TextMuted)
                    }
                }
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GymOrange,
                unfocusedBorderColor = Color(0xFF2A2A3E),
                focusedTextColor = TextWhite,
                unfocusedTextColor = TextWhite,
                cursorColor = GymOrange,
                focusedContainerColor = Color(0xFF12121F),
                unfocusedContainerColor = Color(0xFF12121F)
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // Category chips
        MuscleGroupChipSection(
            onChipSelected = { exerciseViewModel.selectCategory(it) }
        )

        // Exercise list with checkboxes
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(displayList, key = { it.id }) { exercise ->
                val isSelected = exercise.id in selectedIds
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSelected) GymOrange.copy(alpha = 0.1f) else Color(0xFF12121F)
                        )
                        .clickable {
                            selectedIds = if (isSelected) selectedIds - exercise.id
                                         else selectedIds + exercise.id
                        }
                        .padding(10.dp)
                ) {
                    AsyncImage(
                        model = exercise.thumbnail.ifEmpty { null },
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1A1A2E))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = exercise.name.replaceFirstChar { it.uppercase() },
                            color = TextWhite,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        if (exercise.category.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = exercise.category.replaceFirstChar { it.uppercase() },
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                        }
                    }
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = {
                            selectedIds = if (it) selectedIds + exercise.id
                                          else selectedIds - exercise.id
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = GymOrange,
                            uncheckedColor = TextMuted
                        )
                    )
                }
            }
        }
    }
}

// ── Speed Dial ────────────────────────────────────────────────────────────────

@Composable
private fun SpeedDial(
    modifier: Modifier = Modifier,
    isExpanded: Boolean,
    canCopy: Boolean,
    canPaste: Boolean,
    onToggle: () -> Unit,
    onAdd: () -> Unit,
    onCopy: () -> Unit,
    onPaste: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
    ) {
        // Mini FABs — xuất hiện khi mở rộng
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 })
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SpeedDialItem(
                    icon = Icons.Default.ContentPaste,
                    label = stringResource(R.string.btn_paste),
                    enabled = canPaste,
                    onClick = onPaste
                )
                SpeedDialItem(
                    icon = Icons.Default.ContentCopy,
                    label = stringResource(R.string.btn_copy),
                    enabled = canCopy,
                    onClick = onCopy
                )
            }
        }

        // Hàng dưới: mũi tên toggle + nút Add chính
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SmallFloatingActionButton(
                onClick = onToggle,
                containerColor = Color(0xFF2A2A3E),
                contentColor = TextWhite
            ) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown
                                  else Icons.Default.KeyboardArrowUp,
                    contentDescription = null
                )
            }
            FloatingActionButton(
                onClick = onAdd,
                containerColor = GymOrange,
                contentColor = Color.White
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = stringResource(R.string.btn_add_exercise))
            }
        }
    }
}

@Composable
private fun SpeedDialItem(
    icon: ImageVector,
    label: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label,
            color = if (enabled) TextWhite else TextMuted,
            fontSize = 13.sp,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF1A1A2E))
                .padding(horizontal = 10.dp, vertical = 6.dp)
        )
        SmallFloatingActionButton(
            onClick = { if (enabled) onClick() },
            containerColor = if (enabled) Color(0xFF2A2A3E) else Color(0xFF1A1A2E),
            contentColor = if (enabled) TextWhite else TextMuted.copy(alpha = 0.4f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ── Helper ────────────────────────────────────────────────────────────────────

private fun normDate(millis: Long): Long =
    Calendar.getInstance().apply {
        timeInMillis = millis
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
