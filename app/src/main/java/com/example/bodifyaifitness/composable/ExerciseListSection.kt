package com.example.bodifyaifitness.composable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.bodifyaifitness.dataclass.Exercise
import com.example.bodifyaifitness.ui.theme.ChipInactive
import com.example.bodifyaifitness.ui.theme.GymOrange
import com.example.bodifyaifitness.ui.theme.TextMuted
import com.example.bodifyaifitness.ui.theme.TextWhite
import com.example.bodifyaifitness.viewmodel.ExerciseState

@Composable
fun ExerciseListSection(
    state: ExerciseState,
    onExerciseClick: (Exercise) -> Unit = {},
    modifier: Modifier = Modifier
) {
    when (state) {
        is ExerciseState.Loading -> {
            Box(
                contentAlignment = Alignment.Center,
                modifier = modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                CircularProgressIndicator(color = GymOrange)
            }
        }

        is ExerciseState.Error -> {
            Box(
                contentAlignment = Alignment.Center,
                modifier = modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                Text(
                    text = state.message,
                    color = TextMuted,
                    fontSize = 14.sp
                )
            }
        }

        is ExerciseState.Success -> {
            val exercises = state.exercises
            if (exercises.isEmpty()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    Text(
                        text = "Không có bài tập nào",
                        color = TextMuted,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = modifier
                        .fillMaxWidth()
                        .padding(horizontal = 15.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(exercises, key = { it.id }) { exercise ->
                        ExerciseItem(
                            exercise = exercise,
                            onClick = { onExerciseClick(exercise) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(100.dp)) }
                }
            }
        }
    }
}

@Composable
private fun ExerciseItem(exercise: Exercise, onClick: () -> Unit = {}) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .background(ChipInactive)
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        AsyncImage(
            model = exercise.thumbnailUrl.ifEmpty { null },
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF1A1A2E))
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = exercise.name.replaceFirstChar { it.uppercase() },
                color = TextWhite,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            )
            if (exercise.target.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = exercise.target,
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }
        }

        // Category badge
        if (exercise.category.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF12121F))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = exercise.category,
                    color = GymOrange,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
