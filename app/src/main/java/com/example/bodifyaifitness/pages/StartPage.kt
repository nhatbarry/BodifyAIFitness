package com.example.bodifyaifitness.pages

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.bodifyaifitness.R
import com.example.bodifyaifitness.dataclass.Exercise
import com.example.bodifyaifitness.dataclass.ExerciseSet
import com.example.bodifyaifitness.ui.theme.GymOrange
import com.example.bodifyaifitness.ui.theme.GymSurfaceBg
import com.example.bodifyaifitness.ui.theme.TextMuted
import com.example.bodifyaifitness.ui.theme.TextWhite
import com.example.bodifyaifitness.viewmodel.WorkoutLogViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartPage(modifier: Modifier = Modifier) {
    val viewModel: WorkoutLogViewModel = viewModel()
    val isLoading by viewModel.isLoading.collectAsState()
    val todayExercises by viewModel.todayExercises.collectAsState()
    val workoutLog by viewModel.workoutLog.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadTodayData() }

    var selectedExercise by remember { mutableStateOf<Exercise?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val todayFormatted = remember {
        SimpleDateFormat("EEEE, dd/MM/yyyy", Locale.getDefault()).format(Date())
    }

    selectedExercise?.let { exercise ->
        val currentSets = workoutLog?.exercise
            ?.firstOrNull { it.exerciseId == exercise.id }
            ?.sets ?: emptyList()
        ModalBottomSheet(
            onDismissRequest = { selectedExercise = null },
            sheetState = sheetState,
            containerColor = Color(0xFF12121F),
            tonalElevation = 0.dp
        ) {
            ExerciseLogSheet(
                exercise = exercise,
                currentSets = currentSets,
                onSave = { sets -> viewModel.saveSets(exercise.id, exercise.name, sets) }
            )
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = GymSurfaceBg,
        contentWindowInsets = WindowInsets(0)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
        ) {
            // Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(listOf(Color(0xFF0F0F1E), GymSurfaceBg))
                    )
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Text(
                    text = stringResource(R.string.label_today).uppercase(),
                    color = GymOrange,
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp,
                    letterSpacing = 2.sp
                )
                Text(
                    text = todayFormatted,
                    color = TextMuted,
                    fontSize = 13.sp
                )
            }

            when {
                isLoading -> Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = GymOrange)
                }

                todayExercises.isEmpty() -> Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.FitnessCenter,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.empty_today_workout),
                            color = TextWhite,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = stringResource(R.string.empty_today_workout_hint),
                            color = TextMuted,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                else -> LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(todayExercises) { exercise ->
                        val sets = workoutLog?.exercise
                            ?.firstOrNull { it.exerciseId == exercise.id }
                            ?.sets ?: emptyList()
                        ExerciseTrackCard(
                            exercise = exercise,
                            sets = sets,
                            onClick = { selectedExercise = exercise }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExerciseTrackCard(
    exercise: Exercise,
    sets: List<ExerciseSet>,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF12121F)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0D0D0D))
            ) {
                AsyncImage(
                    model = exercise.thumbnail,
                    contentDescription = exercise.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
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
                Spacer(Modifier.height(4.dp))
                if (sets.isEmpty()) {
                    Text(
                        text = stringResource(R.string.label_no_sets_logged),
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                } else {
                    val lastSet = sets.last()
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = stringResource(R.string.label_sets_count, sets.size),
                            color = GymOrange,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = stringResource(R.string.label_last_set, lastSet.weight, lastSet.reps),
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                }
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun ExerciseLogSheet(
    exercise: Exercise,
    currentSets: List<ExerciseSet>,
    onSave: (List<ExerciseSet>) -> Unit
) {
    var sets by remember { mutableStateOf(currentSets.toList()) }
    var weight by remember { mutableStateOf(currentSets.lastOrNull()?.weight ?: 20.0) }
    var reps by remember { mutableStateOf(currentSets.lastOrNull()?.reps ?: 10) }
    // null = adding new set; non-null = editing existing set at that index
    var editingIdx by remember { mutableStateOf<Int?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp)
    ) {
        // Header: exercise name + edit mode indicator
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp)
        ) {
            Text(
                text = exercise.name,
                color = TextWhite,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.weight(1f)
            )
            if (editingIdx != null) {
                IconButton(
                    onClick = {
                        editingIdx = null
                        weight = sets.lastOrNull()?.weight ?: 20.0
                        reps = sets.lastOrNull()?.reps ?: 10
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.Close, null, tint = TextMuted, modifier = Modifier.size(18.dp))
                }
            }
        }

        // Edit mode banner
        if (editingIdx != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(GymOrange.copy(alpha = 0.15f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.Edit, null, tint = GymOrange, modifier = Modifier.size(14.dp))
                Text(
                    text = "Editing set ${editingIdx!! + 1}",
                    color = GymOrange,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(Modifier.height(12.dp))
        }

        // Weight
        Text(
            text = stringResource(R.string.label_weight_kgs),
            color = TextMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 1.sp
        )
        Spacer(Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            StepButton(Icons.Default.Remove) { weight = maxOf(0.0, weight - 2.5) }
            Text(
                text = "%.1f".format(weight),
                color = TextWhite,
                fontWeight = FontWeight.Bold,
                fontSize = 40.sp,
                modifier = Modifier
                    .weight(1f)
                    .wrapContentWidth()
            )
            StepButton(Icons.Default.Add) { weight += 2.5 }
        }

        HorizontalDivider(
            color = Color(0xFF2A2A3E),
            modifier = Modifier.padding(vertical = 16.dp)
        )

        // Reps
        Text(
            text = stringResource(R.string.label_reps),
            color = TextMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 1.sp
        )
        Spacer(Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            StepButton(Icons.Default.Remove) { reps = maxOf(0, reps - 1) }
            Text(
                text = "$reps",
                color = TextWhite,
                fontWeight = FontWeight.Bold,
                fontSize = 40.sp,
                modifier = Modifier
                    .weight(1f)
                    .wrapContentWidth()
            )
            StepButton(Icons.Default.Add) { reps++ }
        }

        Spacer(Modifier.height(24.dp))

        // SAVE / CLEAR buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = {
                    val newSet = ExerciseSet(reps = reps, weight = weight)
                    val newSets = if (editingIdx != null) {
                        sets.toMutableList().also { it[editingIdx!!] = newSet }
                    } else {
                        sets + newSet
                    }
                    sets = newSets
                    onSave(newSets)
                    editingIdx = null
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (editingIdx != null) GymOrange else Color(0xFF00C853)
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = if (editingIdx != null) "Update" else stringResource(R.string.btn_save),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
            OutlinedButton(
                onClick = {
                    weight = 20.0
                    reps = 10
                    editingIdx = null
                },
                border = BorderStroke(1.dp, Color(0xFF2A2A3E)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    stringResource(R.string.btn_clear),
                    color = TextMuted,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }

        // Logged sets list
        if (sets.isNotEmpty()) {
            Spacer(Modifier.height(20.dp))
            HorizontalDivider(color = Color(0xFF2A2A3E))
            Spacer(Modifier.height(12.dp))

            sets.forEachIndexed { idx, set ->
                val isEditing = editingIdx == idx
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isEditing) GymOrange.copy(alpha = 0.1f) else Color.Transparent
                        )
                        .clickable {
                            editingIdx = idx
                            weight = set.weight
                            reps = set.reps
                        }
                        .padding(vertical = 6.dp, horizontal = 4.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(
                                if (isEditing) GymOrange else Color(0xFF2A2A3E)
                            )
                    ) {
                        Text(
                            text = "${idx + 1}",
                            color = if (isEditing) Color.White else TextMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.label_set_row, set.weight, set.reps),
                        color = if (isEditing) GymOrange else TextWhite,
                        fontSize = 14.sp,
                        fontWeight = if (isEditing) FontWeight.Medium else FontWeight.Normal,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = {
                            val newSets = sets.toMutableList().also { it.removeAt(idx) }
                            sets = newSets
                            onSave(newSets)
                            if (editingIdx == idx) editingIdx = null
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null,
                            tint = Color(0xFFFF5555),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StepButton(icon: ImageVector, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(Color(0xFF2A2A3E))
    ) {
        Icon(icon, null, tint = TextWhite, modifier = Modifier.size(22.dp))
    }
}
