package com.example.bodifyaifitness.pages

import android.os.Build
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.example.bodifyaifitness.dataclass.Exercise
import com.example.bodifyaifitness.ui.theme.ChipInactive
import com.example.bodifyaifitness.ui.theme.GymOrange
import com.example.bodifyaifitness.ui.theme.GymSurfaceBg
import com.example.bodifyaifitness.ui.theme.TextMuted
import com.example.bodifyaifitness.ui.theme.TextWhite
import com.example.bodifyaifitness.viewmodel.ExerciseDetailState
import com.example.bodifyaifitness.viewmodel.ExerciseDetailViewModel

@Composable
fun ExerciseDetailPage(
    exerciseId: String,
    navController: NavController,
    detailViewModel: ExerciseDetailViewModel = viewModel()
) {
    val state by detailViewModel.state.collectAsState()

    LaunchedEffect(exerciseId) {
        detailViewModel.loadExercise(exerciseId)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GymSurfaceBg)
    ) {
        when (state) {
            is ExerciseDetailState.Loading -> {
                CircularProgressIndicator(
                    color = GymOrange,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            is ExerciseDetailState.Error -> {
                Text(
                    text = (state as ExerciseDetailState.Error).message,
                    color = TextMuted,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            is ExerciseDetailState.NotFound -> {
                Text(
                    text = "Không tìm thấy bài tập",
                    color = TextMuted,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            is ExerciseDetailState.Success -> {
                val exercise = (state as ExerciseDetailState.Success).exercise
                ExerciseDetailContent(exercise = exercise)
            }
        }

        // Back button (luôn hiển thị bất kể state)
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ExerciseDetailContent(exercise: Exercise) {
    val context = LocalContext.current

    // GIF-capable ImageLoader
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
    ) {
        // ── Thumbnail header ─────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(exercise.thumbnail.ifEmpty { null })
                    .crossfade(true)
                    .build(),
                contentDescription = exercise.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            // Gradient fade vào background
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

        // ── Content ──────────────────────────────────────────────────────────
        Column(
            modifier = Modifier.padding(horizontal = 20.dp)
        ) {

            // Tên bài tập
            Text(
                text = exercise.name.replaceFirstChar { it.uppercase() },
                color = TextWhite,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 26.sp,
                lineHeight = 32.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Category badge
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

            // ── Nhóm cơ tham gia ────────────────────────────────────────────
            if (exercise.muscleGroup.isNotEmpty()) {
                SectionHeader(
                    icon = Icons.Default.LocalFireDepartment,
                    title = "Muscle Groups"
                )
                Spacer(modifier = Modifier.height(10.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    exercise.muscleGroup.forEach { muscle ->
                        MuscleChip(label = muscle.replaceFirstChar { it.uppercase() })
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // ── Dụng cụ ─────────────────────────────────────────────────────
            if (exercise.equipment.isNotEmpty()) {
                SectionHeader(
                    icon = Icons.Default.FitnessCenter,
                    title = "Equipment"
                )
                Spacer(modifier = Modifier.height(10.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    exercise.equipment.forEach { eq ->
                        EquipmentChip(label = eq.replaceFirstChar { it.uppercase() })
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // ── Hướng dẫn ───────────────────────────────────────────────────
            if (exercise.instructionsStep.isNotEmpty()) {
                SectionHeader(
                    icon = Icons.Default.FitnessCenter,
                    title = "Instructions"
                )
                Spacer(modifier = Modifier.height(12.dp))
                exercise.instructionsStep.forEachIndexed { index, step ->
                    Row(
                        verticalAlignment = Alignment.Top,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                    ) {
                        // Số thứ tự
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
            } else if (exercise.instruction.isNotEmpty()) {
                // Fallback: tách câu từ instruction string thành numbered steps
                val steps = exercise.instruction
                    .split(". ")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }

                SectionHeader(
                    icon = Icons.Default.FitnessCenter,
                    title = "Instructions"
                )
                Spacer(modifier = Modifier.height(12.dp))
                steps.forEachIndexed { index, step ->
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
                        // Đảm bảo câu cuối cùng không có dấu chấm thừa
                        val stepText = if (step.endsWith(".")) step else "$step."
                        Text(
                            text = stepText,
                            color = TextMuted,
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // ── GIF bài tập ──────────────────────────────────────────────
            if (exercise.gif.isNotEmpty()) {
                Text(
                    text = "Animation",
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(exercise.gif)
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

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

@Composable
private fun SectionHeader(icon: ImageVector, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = GymOrange,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            color = TextWhite,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
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
        Text(
            text = label,
            color = TextWhite,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
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
        Text(
            text = label,
            color = TextMuted,
            fontSize = 13.sp
        )
    }
}
