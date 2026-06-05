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
import com.example.bodifyaifitness.ui.theme.TextMuted
import com.example.bodifyaifitness.ui.theme.TextWhite
import java.time.DayOfWeek
import java.time.LocalDate

// ── Color mapping: exercise count → intensity color ───────────────────────────
// Màu càng đậm → càng nhiều bài tập trong buổi đó
private fun intensityColor(exerciseCount: Int): Color = when {
    exerciseCount == 0    -> Color(0xFF1A1A2E)   // không tập – nền tối
    exerciseCount == 1    -> Color(0xFF3D1C0A)   // 1 bài – dim ember
    exerciseCount <= 3    -> Color(0xFF7A3B1E)   // 2-3 bài – moderate
    exerciseCount <= 5    -> Color(0xFFB85C2A)   // 4-5 bài – good
    exerciseCount <= 7    -> Color(0xFFE07030)   // 6-7 bài – great
    else                  -> GymOrange           // 8+ bài – 🔥 max
}

// ── Data holder ───────────────────────────────────────────────────────────────
data class StreakDay(val date: LocalDate, val exerciseCount: Int = 0)

private val DAY_LABELS = listOf("Mon", "", "Wed", "", "Fri", "", "Sun")

// ── Main composable ───────────────────────────────────────────────────────────

/**
 * Workout activity heatmap chart.
 *
 * @param activityData  Map of LocalDate → exercise count for that day.
 *                      Days not present in the map are treated as 0 (rest/no log).
 */
@Composable
fun WorkoutStreakChart(
    activityData: Map<LocalDate, Int> = emptyMap(),
    modifier: Modifier = Modifier
) {
    val today = LocalDate.now()
    val start = today.minusWeeks(17L).with(DayOfWeek.MONDAY)

    // Build streakDays for the last 17 weeks
    val streakDays = buildList {
        var current = start
        while (!current.isAfter(today)) {
            add(StreakDay(current, activityData[current] ?: 0))
            current = current.plusDays(1)
        }
    }

    val weeks       = buildWeeks(streakDays)
    val monthLabels = buildMonthLabels(weeks)
    val activeDays  = streakDays.count { it.exerciseCount > 0 }

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
            Column {
                Text(
                    text = "Workout Activity",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextWhite,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Last 17 weeks",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(GymOrange.copy(alpha = 0.15f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "$activeDays sessions",
                    fontSize = 12.sp,
                    color = GymOrange,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // ── Month labels row ─────────────────────────────────────────────────
        Row {
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
                        modifier = Modifier.size(width = 24.dp, height = 12.dp),
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

            // Weeks (columns)
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                weeks.forEach { week ->
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        (0..6).forEach { dayIndex ->
                            val streakDay = week.getOrNull(dayIndex)
                            val count = streakDay?.exerciseCount ?: 0
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(intensityColor(count))
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
            listOf(0, 1, 3, 5, 8).forEach { count ->
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(intensityColor(count))
                )
                Spacer(modifier = Modifier.width(2.dp))
            }
            Text(text = "More", fontSize = 9.sp, color = TextMuted)
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun buildWeeks(days: List<StreakDay>): List<List<StreakDay?>> {
    if (days.isEmpty()) return emptyList()
    val weeks = mutableListOf<List<StreakDay?>>()
    var current = mutableListOf<StreakDay?>()

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
