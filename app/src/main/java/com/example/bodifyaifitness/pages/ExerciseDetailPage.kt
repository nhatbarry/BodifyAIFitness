package com.example.bodifyaifitness.pages

import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.example.bodifyaifitness.R
import com.example.bodifyaifitness.dataclass.Exercise
import com.example.bodifyaifitness.dataclass.ExerciseSet
import com.example.bodifyaifitness.ui.theme.ChipInactive
import com.example.bodifyaifitness.ui.theme.GymOrange
import com.example.bodifyaifitness.ui.theme.GymSurfaceBg
import com.example.bodifyaifitness.ui.theme.TextMuted
import com.example.bodifyaifitness.ui.theme.TextWhite
import com.example.bodifyaifitness.viewmodel.ExerciseDetailState
import com.example.bodifyaifitness.viewmodel.ExerciseDetailViewModel
import com.example.bodifyaifitness.viewmodel.WorkoutLogViewModel
import androidx.activity.ComponentActivity

@Composable
fun ExerciseDetailPage(
    exerciseId: String,
    navController: NavController,
    showLog: Boolean = false,
    detailViewModel: ExerciseDetailViewModel = viewModel()
) {
    // Activity-scoped: cùng instance với StartPage → update real-time khi back
    val activity = LocalContext.current as ComponentActivity
    val workoutLogViewModel: WorkoutLogViewModel = viewModel(activity)
    val state      by detailViewModel.state.collectAsState()
    val workoutLog by workoutLogViewModel.workoutLog.collectAsState()

    LaunchedEffect(exerciseId) {
        detailViewModel.loadExercise(exerciseId)
        if (showLog) workoutLogViewModel.loadTodayData()
    }

    // Box wraps Scaffold so back button can overlay on top of everything
    Box(modifier = Modifier.fillMaxSize()) {

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = GymSurfaceBg,
            contentWindowInsets = WindowInsets(0),
            bottomBar = {
                // Log panel cố định ở bottom - chỉ khi showLog=true & exercise loaded
                if (showLog && state is ExerciseDetailState.Success) {
                    val exercise = (state as ExerciseDetailState.Success).exercise
                    val currentSets = workoutLog?.exercise
                        ?.firstOrNull { it.exerciseId == exerciseId }
                        ?.sets ?: emptyList()
                    LogBottomPanel(
                        currentSets = currentSets,
                        onSave      = { sets ->
                            workoutLogViewModel.saveSets(exerciseId, exercise.name, sets)
                        }
                    )
                }
            }
        ) { innerPadding ->
            when (state) {
                is ExerciseDetailState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(innerPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = GymOrange)
                    }
                }

                is ExerciseDetailState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(innerPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (state as ExerciseDetailState.Error).message,
                            color = TextMuted
                        )
                    }
                }

                is ExerciseDetailState.NotFound -> {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(innerPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.error_exercise_not_found),
                            color = TextMuted
                        )
                    }
                }

                is ExerciseDetailState.Success -> {
                    val exercise = (state as ExerciseDetailState.Success).exercise
                    ExerciseDetailContent(
                        exercise       = exercise,
                        bottomPadding  = innerPadding.calculateBottomPadding()
                    )
                }
            }
        }

        // Back button luôn overlay trên cùng
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .statusBarsPadding()
                .padding(8.dp)
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.45f))
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBackIosNew,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

// ── Exercise detail content (scrollable) ──────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ExerciseDetailContent(
    exercise: Exercise,
    bottomPadding: androidx.compose.ui.unit.Dp = 0.dp
) {
    val context = LocalContext.current

    val isVi = AppCompatDelegate.getApplicationLocales().toLanguageTags().startsWith("vi")
    val langKey = if (isVi) "vie" else "en"
    val instructionSteps = exercise.instructionSteps[langKey]
        ?: exercise.instructionSteps["en"]
        ?: emptyList()

    val gifImageLoader = ImageLoader.Builder(context)
        .components {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                add(ImageDecoderDecoder.Factory())
            } else {
                add(GifDecoder.Factory())
            }
        }
        .build()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = bottomPadding)
    ) {
        // ── Thumbnail header ──────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(exercise.thumbnailUrl.ifEmpty { null })
                    .crossfade(true)
                    .build(),
                contentDescription = exercise.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                GymSurfaceBg.copy(alpha = 0.6f),
                                GymSurfaceBg
                            ),
                            startY = 200f
                        )
                    )
            )
        }

        // ── Content ───────────────────────────────────────────────────────────
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {

            Text(
                text = exercise.name.replaceFirstChar { it.uppercase() },
                color = TextWhite,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 26.sp,
                lineHeight = 32.sp
            )
            Spacer(modifier = Modifier.height(4.dp))

            if (exercise.category.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(GymOrange.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = exercise.category.replaceFirstChar { it.uppercase() },
                        color = GymOrange,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Nhóm cơ
            val allMuscles = buildList {
                if (exercise.muscleGroup.isNotEmpty()) add(exercise.muscleGroup)
                addAll(exercise.secondaryMuscles)
            }.distinct()
            if (allMuscles.isNotEmpty()) {
                SectionHeader(Icons.Default.LocalFireDepartment, stringResource(R.string.label_muscle_groups))
                Spacer(modifier = Modifier.height(10.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    allMuscles.forEach { muscle ->
                        MuscleChip(label = muscle.replaceFirstChar { it.uppercase() })
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Dụng cụ
            if (exercise.equipment.isNotEmpty()) {
                SectionHeader(Icons.Default.FitnessCenter, stringResource(R.string.label_equipment))
                Spacer(modifier = Modifier.height(10.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    EquipmentChip(label = exercise.equipment.replaceFirstChar { it.uppercase() })
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Hướng dẫn
            if (instructionSteps.isNotEmpty()) {
                SectionHeader(Icons.Default.FitnessCenter, stringResource(R.string.label_instructions))
                Spacer(modifier = Modifier.height(12.dp))
                instructionSteps.forEachIndexed { index, step ->
                    Row(
                        verticalAlignment = Alignment.Top,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(GymOrange)
                        ) {
                            Text(
                                text = "${index + 1}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = step,
                            color = TextMuted,
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // GIF
            if (exercise.gifUrl.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.label_animation),
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(exercise.gifUrl)
                        .crossfade(false)
                        .build(),
                    imageLoader = gifImageLoader,
                    contentDescription = "${exercise.name} animation",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF12121F))
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ── Log bottom panel (cố định ở bottom) ──────────────────────────────────────

@Composable
private fun LogBottomPanel(
    currentSets: List<ExerciseSet>,
    onSave: (List<ExerciseSet>) -> Unit
) {
    var sets       by remember { mutableStateOf(currentSets.toList()) }
    var weight     by remember { mutableStateOf(currentSets.lastOrNull()?.weight ?: 20.0) }
    var reps       by remember { mutableStateOf(currentSets.lastOrNull()?.reps ?: 10) }
    var editingIdx by remember { mutableStateOf<Int?>(null) }

    // Sync khi data từ Firestore arrive lần đầu
    LaunchedEffect(currentSets) {
        sets = currentSets.toList()
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF0A0A15),
        shadowElevation = 24.dp,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .padding(top = 12.dp, bottom = 24.dp)
        ) {
            // Drag handle indicator
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF3A3A5A))
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(Modifier.height(14.dp))

            // Edit mode banner
            if (editingIdx != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(GymOrange.copy(alpha = 0.12f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Edit, null, tint = GymOrange, modifier = Modifier.size(13.dp))
                    Text(
                        text = "Editing set ${editingIdx!! + 1}",
                        color = GymOrange,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = {
                            editingIdx = null
                            weight = sets.lastOrNull()?.weight ?: 20.0
                            reps   = sets.lastOrNull()?.reps ?: 10
                        },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(Icons.Default.Close, null, tint = TextMuted, modifier = Modifier.size(14.dp))
                    }
                }
                Spacer(Modifier.height(10.dp))
            }

            // ── Weight + Reps side by side ────────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Weight
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = stringResource(R.string.label_weight_kgs),
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CompactStepBtn(Icons.Default.Remove) { weight = maxOf(0.0, weight - 2.5) }
                        Text(
                            text = "%.1f".format(weight),
                            color = TextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                        CompactStepBtn(Icons.Default.Add) { weight += 2.5 }
                    }
                }

                // Separator
                Box(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .width(1.dp)
                        .height(52.dp)
                        .background(Color(0xFF2A2A3E))
                )

                // Reps
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = stringResource(R.string.label_reps),
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CompactStepBtn(Icons.Default.Remove) { reps = maxOf(0, reps - 1) }
                        Text(
                            text = "$reps",
                            color = TextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                        CompactStepBtn(Icons.Default.Add) { reps++ }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // ── Save / Clear ──────────────────────────────────────────────────
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
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 10.dp)
                ) {
                    Text(
                        text = if (editingIdx != null) stringResource(R.string.btn_update)
                               else stringResource(R.string.btn_save),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
                OutlinedButton(
                    onClick = { weight = 20.0; reps = 10; editingIdx = null },
                    border = BorderStroke(1.dp, Color(0xFF2A2A3E)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 10.dp)
                ) {
                    Text(
                        stringResource(R.string.btn_clear),
                        color = TextMuted,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            // ── Set chips (horizontal scrollable) ─────────────────────────────
            if (sets.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = Color(0xFF1E1E30))
                Spacer(Modifier.height(10.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    itemsIndexed(sets) { idx, set ->
                        val isEditing = editingIdx == idx
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    if (isEditing) GymOrange
                                    else Color(0xFF1E1E3A)
                                )
                                .clickable {
                                    editingIdx = idx
                                    weight = set.weight
                                    reps = set.reps
                                }
                                .padding(start = 12.dp, end = 8.dp, top = 6.dp, bottom = 6.dp)
                        ) {
                            Text(
                                text = "${idx + 1} · ${"%.0f".format(set.weight)}kg×${set.reps}",
                                color = if (isEditing) Color.White else TextMuted,
                                fontSize = 12.sp,
                                fontWeight = if (isEditing) FontWeight.Bold else FontWeight.Normal
                            )
                            Spacer(Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isEditing) Color.White.copy(alpha = 0.2f)
                                        else Color(0xFF2A2A40)
                                    )
                                    .clickable {
                                        val newSets = sets.toMutableList().also { it.removeAt(idx) }
                                        sets = newSets
                                        onSave(newSets)
                                        if (editingIdx == idx) editingIdx = null
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = null,
                                    tint = if (isEditing) Color.White else TextMuted,
                                    modifier = Modifier.size(10.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactStepBtn(icon: ImageVector, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(Color(0xFF1E1E3A))
    ) {
        Icon(icon, null, tint = TextWhite, modifier = Modifier.size(18.dp))
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

@Composable
private fun SectionHeader(icon: ImageVector, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null, tint = GymOrange, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = title, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

@Composable
private fun MuscleChip(label: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF1E1E2E))
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Text(text = label, color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun EquipmentChip(label: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(ChipInactive)
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Text(text = label, color = TextMuted, fontSize = 13.sp)
    }
}
