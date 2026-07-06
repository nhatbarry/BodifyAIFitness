package com.example.bodifyaifitness.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.bodifyaifitness.R
import com.example.bodifyaifitness.ui.theme.ChipActive
import com.example.bodifyaifitness.ui.theme.ChipInactive
import com.example.bodifyaifitness.ui.theme.TextMuted
import com.example.bodifyaifitness.ui.theme.TextWhite

/**
 * Toggle chip lọc riêng các bài tập có hỗ trợ camera AI đếm rep (Exercise.isAISupported).
 * Độc lập với MuscleGroupChipSection (nhóm cơ) — đây là 1 công tắc bật/tắt, vị trí cố định.
 */
@Composable
fun AiCameraFilterChip(
    selected: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .padding(start = 15.dp, top = 15.dp, bottom = 15.dp)
            .clickable { onToggle() }
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) ChipActive else ChipInactive)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Videocam,
                contentDescription = null,
                tint = if (selected) TextWhite else TextMuted,
                modifier = Modifier.padding(end = 4.dp)
            )
            Text(
                text = stringResource(R.string.filter_ai_camera),
                color = if (selected) TextWhite else TextMuted,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

/**
 * Multi-select: có thể chọn nhiều nhóm cơ cùng lúc (lọc kiểu OR). "All" là nút reset —
 * chọn "All" bỏ hết các nhóm khác; chọn 1 nhóm bất kỳ thì "All" tự bỏ chọn.
 * Chip đang được chọn sẽ tự nhảy lên đầu danh sách (trừ "All" luôn đứng đầu tiên).
 */
@Composable
fun MuscleGroupChipSection(
    muscleGroups: List<String> = listOf(
        "All", "Waist", "Upper Legs", "Back", "Lower Legs",
        "Chest", "Upper Arms", "Cardio", "Shoulders", "Lower Arms", "Neck"
    ),
    selectedCategories: Set<String> = emptySet(),
    onChipToggled: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val orderedGroups = remember(selectedCategories, muscleGroups) {
        val (all, rest) = muscleGroups.partition { it == "All" }
        val (selected, unselected) = rest.partition { it in selectedCategories }
        all + selected + unselected
    }

    LazyRow(modifier = modifier) {
        items(orderedGroups, key = { it }) { group ->
            val isSelected = if (group == "All") selectedCategories.isEmpty() else group in selectedCategories
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .padding(start = 15.dp, top = 15.dp, bottom = 15.dp)
                    .clickable { onChipToggled(group) }
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) ChipActive else ChipInactive)
                    .padding(horizontal = 15.dp, vertical = 10.dp)
            ) {
                Text(
                    text = group,
                    color = if (isSelected) TextWhite else TextMuted,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
