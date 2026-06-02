package com.example.bodifyaifitness.dataclass

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class WorkoutCategory(
    val title: String,
    val icon: ImageVector,
    val lightColor: Color,
    val mediumColor: Color,
    val darkColor: Color
)
