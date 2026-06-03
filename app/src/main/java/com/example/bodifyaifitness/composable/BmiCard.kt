package com.example.bodifyaifitness.composable

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.example.bodifyaifitness.R
import com.example.bodifyaifitness.ui.theme.GymOrange
import com.example.bodifyaifitness.ui.theme.TextMuted
import com.example.bodifyaifitness.ui.theme.TextWhite

// ── BMI category helpers ──────────────────────────────────────────────────────

data class BmiCategory(
    val labelRes: Int,
    val color: Color,
    val range: ClosedFloatingPointRange<Float>
)

private val bmiCategories = listOf(
    BmiCategory(R.string.bmi_underweight, Color(0xFF5DADE2), 0f..18.5f),
    BmiCategory(R.string.bmi_normal,      Color(0xFF2ECC71), 18.5f..24.9f),
    BmiCategory(R.string.bmi_overweight,  Color(0xFFF39C12), 24.9f..29.9f),
    BmiCategory(R.string.bmi_obese,       Color(0xFFE74C3C), 29.9f..60f)
)

private fun getBmiCategory(bmi: Float): BmiCategory =
    bmiCategories.firstOrNull { bmi in it.range } ?: bmiCategories.last()

/** Maps a BMI value (10–40) to a 0f–1f fraction for the gauge bar. */
private fun bmiToFraction(bmi: Float): Float =
    ((bmi - 10f) / 30f).coerceIn(0f, 1f)

// ── Composable ────────────────────────────────────────────────────────────────

/**
 * Displays a gradient BMI gauge bar + metadata.
 *
 * @param bmi   calculated BMI value; if null, shows an "add data" prompt.
 * @param heightCm user height in cm
 * @param weightKg user weight in kg
 */
@Composable
fun BmiCard(
    bmi: Float?,
    heightCm: Float = 0f,
    weightKg: Float = 0f,
    modifier: Modifier = Modifier
) {
    // Animate bar fill on first draw
    var triggered by remember { mutableStateOf(false) }
    val animatedFraction by animateFloatAsState(
        targetValue = if (triggered && bmi != null) bmiToFraction(bmi) else 0f,
        animationSpec = tween(durationMillis = 900),
        label = "bmi_bar"
    )
    LaunchedEffect(bmi) { triggered = true }

    val category = bmi?.let { getBmiCategory(it) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF12121F))
            .padding(16.dp)
    ) {
        // ── Header row ──────────────────────────────────────────────────────
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(GymOrange.copy(alpha = 0.15f))
                ) {
                    Icon(
                        imageVector = Icons.Default.MonitorWeight,
                        contentDescription = null,
                        tint = GymOrange,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = stringResource(R.string.title_bmi),
                    style = MaterialTheme.typography.titleLarge,
                    color = TextWhite
                )
            }

            if (category != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(category.color.copy(alpha = 0.2f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = stringResource(category.labelRes),
                        color = category.color,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (bmi == null) {
            // ── No data state ────────────────────────────────────────────
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "—",
                    color = TextMuted,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.bmi_no_data),
                    color = TextMuted,
                    fontSize = 13.sp
                )
            }
        } else {
            val cat = getBmiCategory(bmi)   // bmi != null here, always returns non-null

            // ── BMI value ────────────────────────────────────────────────
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "%.1f".format(bmi),
                    color = cat.color,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Black,
                    lineHeight = 48.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.unit_bmi),
                    color = TextMuted,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Gradient gauge bar ───────────────────────────────────────
            GaugeBar(fraction = animatedFraction, indicatorColor = cat.color)

            Spacer(modifier = Modifier.height(8.dp))

            // Range labels
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf("10", "18.5", "25", "30", "40+").forEach { label ->
                    Text(text = label, color = TextMuted, fontSize = 10.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Metrics row ──────────────────────────────────────────────
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                MetricChip(label = stringResource(R.string.label_height_short), value = "${heightCm.toInt()} cm")
                MetricChip(label = stringResource(R.string.label_weight_short), value = "${weightKg.toInt()} kg")
                MetricChip(label = stringResource(R.string.label_ideal_weight), value = idealWeightRange(heightCm))
            }
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

@Composable
private fun GaugeBar(fraction: Float, indicatorColor: Color) {
    val gaugeGradient = Brush.horizontalGradient(
        0f   to Color(0xFF5DADE2),   // Underweight – blue
        0.28f to Color(0xFF2ECC71), // Normal – green
        0.5f to Color(0xFFF39C12),  // Overweight – amber
        1f   to Color(0xFFE74C3C)   // Obese – red
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(RoundedCornerShape(50))
            .background(Color(0xFF2A2A3E))
    ) {
        // Coloured fill
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction)
                .height(10.dp)
                .clip(RoundedCornerShape(50))
                .background(gaugeGradient)
        )
        // Indicator dot
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction)
                .height(10.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(indicatorColor)
            )
        }
    }
}

@Composable
private fun MetricChip(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF1E1E2E))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Text(text = value, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = label, color = TextMuted, fontSize = 11.sp)
    }
}

/** Rough ideal weight range by Devine formula (±2 kg). */
private fun idealWeightRange(heightCm: Float): String {
    if (heightCm <= 0f) return "—"
    val base = if (heightCm <= 152f) 50f
    else 50f + 0.9f * (heightCm - 152f)
    return "${(base - 2f).toInt()}–${(base + 2f).toInt()} kg"
}
