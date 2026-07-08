package com.example.bodifyaifitness.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.example.bodifyaifitness.R
import com.example.bodifyaifitness.ui.theme.ChipInactive
import com.example.bodifyaifitness.ui.theme.GymOrange
import com.example.bodifyaifitness.ui.theme.TextMuted
import com.example.bodifyaifitness.ui.theme.TextWhite

@Composable
fun GreetingSection(
    name: String = "Athlete",
    todayExerciseCount: Int = 0,
    onBellClick: () -> Unit = {}
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 15.dp, vertical = 15.dp)
    ) {
        Column(verticalArrangement = Arrangement.Center) {
            Text(
                text = stringResource(R.string.greeting_with_name, name),
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = stringResource(R.string.greeting_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted
            )
        }

        // ── Bell button với badge ─────────────────────────────────────────────
        Box {
            IconButton(
                onClick = onBellClick,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(ChipInactive)
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = stringResource(R.string.content_desc_notification),
                    tint = if (todayExerciseCount > 0) GymOrange else TextWhite,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Badge chấm cam hiển thị khi có bài tập hôm nay
            if (todayExerciseCount > 0) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = (-2).dp, y = 2.dp)
                        .clip(CircleShape)
                        .background(GymOrange)
                )
            }
        }
    }
}
