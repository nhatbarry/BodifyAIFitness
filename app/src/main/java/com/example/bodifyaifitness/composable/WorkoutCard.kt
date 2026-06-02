package com.example.bodifyaifitness.composable

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bodifyaifitness.dataclass.WorkoutCategory
import com.example.bodifyaifitness.ui.theme.GymOrange
import com.example.bodifyaifitness.ui.theme.TextWhite
import com.example.bodifyaifitness.utils.standardQuadFromTo

@Composable
fun WorkoutCard(
    category: WorkoutCategory
) {
    BoxWithConstraints(
        modifier = Modifier
            .padding(vertical = 6.dp)
            .fillMaxWidth()
            .height(100.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(category.darkColor)
    ) {
        val width = constraints.maxWidth.toFloat()
        val height = constraints.maxHeight.toFloat()

        val mediumPath = Path().apply {
            moveTo(0f, height * 0.5f)
            standardQuadFromTo(Offset(0f, height * 0.5f), Offset(width * 0.15f, height * 0.4f))
            standardQuadFromTo(Offset(width * 0.15f, height * 0.4f), Offset(width * 0.5f, height * 0.6f))
            standardQuadFromTo(Offset(width * 0.5f, height * 0.6f), Offset(width * 0.85f, height * 0.2f))
            standardQuadFromTo(Offset(width * 0.85f, height * 0.2f), Offset(width * 1.2f, height * 0.5f))
            lineTo(width + 100f, height + 100f)
            lineTo(-100f, height + 100f)
            close()
        }

        val lightPath = Path().apply {
            moveTo(0f, height * 0.65f)
            standardQuadFromTo(Offset(0f, height * 0.65f), Offset(width * 0.2f, height * 0.55f))
            standardQuadFromTo(Offset(width * 0.2f, height * 0.55f), Offset(width * 0.55f, height * 0.75f))
            standardQuadFromTo(Offset(width * 0.55f, height * 0.75f), Offset(width * 0.9f, height * 0.4f))
            standardQuadFromTo(Offset(width * 0.9f, height * 0.4f), Offset(width * 1.2f, height * 0.65f))
            lineTo(width + 100f, height + 100f)
            lineTo(-100f, height + 100f)
            close()
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            drawPath(path = mediumPath, color = category.mediumColor)
            drawPath(path = lightPath, color = category.lightColor)
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.25f))
            ) {
                Icon(
                    imageVector = category.icon,
                    contentDescription = category.title,
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = category.title,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = "Start",
                color = TextWhite,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clickable { /* TODO: navigate to workout */ }
                    .clip(RoundedCornerShape(10.dp))
                    .background(GymOrange)
                    .padding(vertical = 8.dp, horizontal = 18.dp)
            )
        }
    }
}
