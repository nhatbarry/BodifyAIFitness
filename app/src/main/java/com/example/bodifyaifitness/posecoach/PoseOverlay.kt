package com.example.bodifyaifitness.posecoach

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.google.mlkit.vision.pose.PoseLandmark
import kotlin.math.max

private const val MIN_LIKELIHOOD = 0.5f

private val BONES = listOf(
    PoseLandmark.LEFT_SHOULDER to PoseLandmark.RIGHT_SHOULDER,
    PoseLandmark.LEFT_SHOULDER to PoseLandmark.LEFT_ELBOW,
    PoseLandmark.LEFT_ELBOW to PoseLandmark.LEFT_WRIST,
    PoseLandmark.RIGHT_SHOULDER to PoseLandmark.RIGHT_ELBOW,
    PoseLandmark.RIGHT_ELBOW to PoseLandmark.RIGHT_WRIST,
    PoseLandmark.LEFT_SHOULDER to PoseLandmark.LEFT_HIP,
    PoseLandmark.RIGHT_SHOULDER to PoseLandmark.RIGHT_HIP,
    PoseLandmark.LEFT_HIP to PoseLandmark.RIGHT_HIP,
    PoseLandmark.LEFT_HIP to PoseLandmark.LEFT_KNEE,
    PoseLandmark.LEFT_KNEE to PoseLandmark.LEFT_ANKLE,
    PoseLandmark.RIGHT_HIP to PoseLandmark.RIGHT_KNEE,
    PoseLandmark.RIGHT_KNEE to PoseLandmark.RIGHT_ANKLE
)

/**
 * Draws the detected skeleton on top of the CameraX preview.
 *
 * Assumes the PreviewView uses the default FILL_CENTER scale type (crop-to-fill): the
 * scale/offset formula below (scale = max of width/height ratios, then center the leftover)
 * mirrors Google's ML Kit CameraX quickstart `GraphicOverlay` reference implementation, which
 * this same-shaped math is copied from.
 */
@Composable
fun PoseOverlay(
    frame: PoseFrame?,
    isFrontCamera: Boolean,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val f = frame ?: return@Canvas
        if (f.imageWidth == 0 || f.imageHeight == 0) return@Canvas

        val scale = max(size.width / f.imageWidth, size.height / f.imageHeight)
        val offsetX = (size.width - f.imageWidth * scale) / 2f
        val offsetY = (size.height - f.imageHeight * scale) / 2f

        fun mapPoint(x: Float, y: Float): Offset {
            val scaledX = x * scale + offsetX
            val mappedX = if (isFrontCamera) size.width - scaledX else scaledX
            return Offset(mappedX, y * scale + offsetY)
        }

        val pose = f.pose

        BONES.forEach { (startType, endType) ->
            val start = pose.getPoseLandmark(startType)
            val end = pose.getPoseLandmark(endType)
            if (start != null && end != null &&
                start.inFrameLikelihood > MIN_LIKELIHOOD && end.inFrameLikelihood > MIN_LIKELIHOOD
            ) {
                drawLine(
                    color = Color(0xFFFF6B35),
                    start = mapPoint(start.position.x, start.position.y),
                    end = mapPoint(end.position.x, end.position.y),
                    strokeWidth = 6f
                )
            }
        }

        pose.allPoseLandmarks.forEach { landmark ->
            if (landmark.inFrameLikelihood > MIN_LIKELIHOOD) {
                drawCircle(
                    color = Color.White,
                    radius = 6f,
                    center = mapPoint(landmark.position.x, landmark.position.y)
                )
            }
        }
    }
}
