package com.example.bodifyaifitness.pages

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.LocalContext
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

@Composable
fun StartPage(modifier: Modifier = Modifier, navController: NavController) {
    val activity = LocalContext.current as ComponentActivity
    val viewModel: WorkoutLogViewModel = viewModel(activity)
    val isLoading      by viewModel.isLoading.collectAsState()
    val todayExercises by viewModel.todayExercises.collectAsState()
    val workoutLog     by viewModel.workoutLog.collectAsState()

    val todayFormatted = remember {
        SimpleDateFormat("EEEE, dd/MM/yyyy", Locale.getDefault()).format(Date())
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
            // ── Header ────────────────────────────────────────────────────────
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

            // ── Content ───────────────────────────────────────────────────────
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
                            onClick = {
                                navController.navigate("exercise_detail/${exercise.id}?showLog=true")
                            }
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
                    model = exercise.thumbnailUrl,
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
                    val totalSets  = sets.size
                    val totalReps  = sets.sumOf { it.reps }
                    val volume     = sets.sumOf { it.weight * it.reps }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        StatChip(text = "$totalSets sets", bgColor = Color(0xFF1E2E1E), textColor = Color(0xFF4CAF50))
                        StatChip(text = "$totalReps reps", bgColor = Color(0xFF1A1A2E), textColor = Color(0xFF7986CB))
                        StatChip(text = "${"%.0f".format(volume)}kg", bgColor = Color(0xFF2A1E10), textColor = GymOrange)
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
private fun StatChip(text: String, bgColor: Color, textColor: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 7.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
