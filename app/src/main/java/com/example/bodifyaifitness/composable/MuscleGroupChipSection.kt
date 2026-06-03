package com.example.bodifyaifitness.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringArrayResource
import com.example.bodifyaifitness.R
import com.example.bodifyaifitness.ui.theme.ChipActive
import com.example.bodifyaifitness.ui.theme.ChipInactive
import com.example.bodifyaifitness.ui.theme.TextMuted
import com.example.bodifyaifitness.ui.theme.TextWhite

@Composable
fun MuscleGroupChipSection(
    muscleGroups: List<String> = listOf(
        "All", "Waist", "Upper Legs", "Back", "Lower Legs",
        "Chest", "Upper Arms", "Cardio", "Shoulders", "Lower Arms", "Neck"
    ),
    onChipSelected: (String) -> Unit = {}
) {
    var selectedChipIndex by remember { mutableStateOf(0) }

    LazyRow {
        items(muscleGroups.size) { index ->
            val isSelected = selectedChipIndex == index
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .padding(start = 15.dp, top = 15.dp, bottom = 15.dp)
                    .clickable {
                        selectedChipIndex = index
                        onChipSelected(muscleGroups[index])
                    }
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) ChipActive else ChipInactive)
                    .padding(horizontal = 15.dp, vertical = 10.dp)
            ) {
                Text(
                    text = muscleGroups[index],
                    color = if (isSelected) TextWhite else TextMuted,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
