package com.example.bodifyaifitness.composable

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bodifyaifitness.ui.theme.GymOrange
import com.example.bodifyaifitness.ui.theme.GymSurfaceBg
import com.example.bodifyaifitness.ui.theme.TextMuted
import com.example.bodifyaifitness.ui.theme.TextWhite
import java.time.DayOfWeek
import java.time.LocalDate

// Intensity levels 0–4 mapped to colors
private fun intensityColor(level: Int): Color = when (level) {
    0 -> Color(0xFF1E1E2E)   // no workout – very dark
    1 -> Color(0xFF7A3B1E)   // light – dim ember
    2 -> Color(0xFFB85C2A)   // moderate
    3 -> Color(0xFFE07030)   // good
    4 -> GymOrange           // 🔥 max – GymOrange
    else -> Color(0xFF1E1E2E)
}

/**
 * Data holder: one entry per day.
 * [level] 0 = rest, 1–4 = workout intensity.
 */
data class StreakDay(val date: LocalDate, val level: Int = 0)

/**
 * Generates placeholder streak data for the past [weeks] weeks.
 * Replace with real data from ViewModel later.
 */
fun generatePlaceholderStreak(weeks: Int = 17): List<StreakDay> {
    val today = LocalDate.now()
    val start = today.minusWeeks(weeks.toLong()).with(DayOfWeek.MONDAY)
    val days = mutableListOf<StreakDay>()
    var current = start
    while (!current.isAfter(today)) {
        // Scatter some random-looking but deterministic levels
        val seed = current.dayOfYear * current.year
        val level = when {
            seed % 7 == 0 -> 4
            seed % 5 == 0 -> 3
            seed % 3 == 0 -> 2
            seed % 2 == 0 -> 1
            else -> 0
        }
        days.add(StreakDay(current, level))
        current = current.plusDays(1)
    }
    return days
}

private val DAY_LABELS = listOf("Mon", "", "Wed", "", "Fri", "", "Sun")

@Composable
fun WorkoutStreakChart(
    streakDays: List<StreakDay> = generatePlaceholderStreak(),
    modifier: Modifier = Modifier
) {
    // Group into weeks (columns of 7 days, Mon–Sun)
    val weeks: List<List<StreakDay?>> = buildWeeks(streakDays)

    // Month labels: collect which column each new month starts at
    val monthLabels = buildMonthLabels(weeks)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF12121F))
            .padding(16.dp)
    ) {
        // ── Header ────────────────────────────────────────────────────────────
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Workout Streak",
                style = MaterialTheme.typography.titleLarge,
                color = TextWhite
            )
            val totalActive = streakDays.count { it.level > 0 }
            Text(
                text = "$totalActive days",
                fontSize = 13.sp,
                color = GymOrange,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ── Month labels row ─────────────────────────────────────────────────
        Row {
            // Left offset for day-of-week labels
            Spacer(modifier = Modifier.width(24.dp))
            weeks.forEachIndexed { weekIdx, _ ->
                val label = monthLabels[weekIdx]
                Box(modifier = Modifier.width(14.dp)) {
                    if (label != null) {
                        Text(
                            text = label,
                            fontSize = 9.sp,
                            color = TextMuted,
                            maxLines = 1
                        )
                    }
                }
                Spacer(modifier = Modifier.width(2.dp))
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // ── Grid: day-of-week rows × week columns ─────────────────────────
        Row {
            // Day-of-week labels (left column)
            Column(
                modifier = Modifier.width(24.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                DAY_LABELS.forEach { label ->
                    Box(
                        modifier = Modifier
                            .size(width = 24.dp, height = 12.dp),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Text(
                            text = label,
                            fontSize = 8.sp,
                            color = TextMuted,
                            lineHeight = 8.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Weeks
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                weeks.forEach { week ->
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        // 7 day cells per column
                        (0..6).forEach { dayIndex ->
                            val streakDay = week.getOrNull(dayIndex)
                            val level = streakDay?.level ?: 0
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(intensityColor(level))
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ── Legend ───────────────────────────────────────────────────────────
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Less", fontSize = 9.sp, color = TextMuted)
            Spacer(modifier = Modifier.width(4.dp))
            (0..4).forEach { level ->
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(intensityColor(level))
                )
                Spacer(modifier = Modifier.width(2.dp))
            }
            Text(text = "More", fontSize = 9.sp, color = TextMuted)
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

/**
 * Groups flat StreakDay list into columns of 7 (Mon=0 … Sun=6).
 * Pads the first week with nulls if it doesn't start on Monday.
 */
private fun buildWeeks(days: List<StreakDay>): List<List<StreakDay?>> {
    if (days.isEmpty()) return emptyList()
    val weeks = mutableListOf<List<StreakDay?>>()
    var current = mutableListOf<StreakDay?>()

    // Pad first week
    val firstDow = days.first().date.dayOfWeek.value - 1 // Mon=0
    repeat(firstDow) { current.add(null) }

    days.forEach { day ->
        current.add(day)
        if (current.size == 7) {
            weeks.add(current.toList())
            current = mutableListOf()
        }
    }
    if (current.isNotEmpty()) {
        while (current.size < 7) current.add(null)
        weeks.add(current.toList())
    }
    return weeks
}

/**
 * Returns a map of weekIndex → month abbreviation, only where a new month starts.
 */
private fun buildMonthLabels(weeks: List<List<StreakDay?>>): Map<Int, String?> {
    val map = mutableMapOf<Int, String?>()
    val monthNames = listOf("Jan","Feb","Mar","Apr","May","Jun",
        "Jul","Aug","Sep","Oct","Nov","Dec")
    var lastMonth = -1
    weeks.forEachIndexed { idx, week ->
        val firstDay = week.firstNotNullOfOrNull { it }
        val month = firstDay?.date?.monthValue ?: -1
        if (month != -1 && month != lastMonth) {
            map[idx] = monthNames[month - 1]
            lastMonth = month
        } else {
            map[idx] = null
        }
    }
    return map
}
