package com.example.bodifyaifitness.posecoach

import android.graphics.PointF
import com.google.mlkit.vision.pose.PoseLandmark
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sqrt

/** Below this, ML Kit itself isn't confident about the landmark — an off-angle camera or a
 *  partially out-of-frame body produces exactly this, and trusting the position anyway is
 *  what causes spurious rep counts. */
const val MIN_LANDMARK_CONFIDENCE = 0.65f

/**
 * Geometry helpers shared by every [ExerciseProcessor]. All angles are computed from
 * ML Kit's 2D image-space landmark positions, which is what the rep-counting thresholds
 * in the exercise processors are tuned against.
 */
object PoseAngleUtils {

    /** True only if every given landmark is above [MIN_LANDMARK_CONFIDENCE]. Call this before
     *  trusting a landmark's position for angle math — low confidence means ML Kit is
     *  effectively guessing, which is exactly when a bad camera angle produces a false rep. */
    fun allConfident(vararg landmarks: PoseLandmark, minConfidence: Float = MIN_LANDMARK_CONFIDENCE): Boolean =
        landmarks.all { it.inFrameLikelihood >= minConfidence }

    /** Angle at [mid], formed by rays mid->first and mid->last, clamped to [0, 180] degrees. */
    fun angleOf(first: PoseLandmark, mid: PoseLandmark, last: PoseLandmark): Double =
        angleOf(first.position, mid.position, last.position)

    private fun angleOf(first: PointF, mid: PointF, last: PointF): Double {
        var degrees = Math.toDegrees(
            (atan2((last.y - mid.y).toDouble(), (last.x - mid.x).toDouble()) -
                atan2((first.y - mid.y).toDouble(), (first.x - mid.x).toDouble()))
        )
        degrees = abs(degrees)
        return if (degrees > 180.0) 360.0 - degrees else degrees
    }

    /** Euclidean distance between two landmarks in image-space pixels. */
    fun distance(a: PoseLandmark, b: PoseLandmark): Float {
        val dx = a.position.x - b.position.x
        val dy = a.position.y - b.position.y
        return sqrt(dx * dx + dy * dy)
    }

    /**
     * Signed vertical deviation of [point] from the straight line between [lineStart] and
     * [lineEnd], measured at point's x-coordinate and normalized by the line's own length
     * so the result is resolution/distance independent (roughly -1..1 in practice).
     *
     * Positive = [point] sags below the line (larger y, towards the floor).
     * Negative = [point] is lifted above the line (piking).
     */
    fun normalizedVerticalDeviationFromLine(
        lineStart: PoseLandmark,
        point: PoseLandmark,
        lineEnd: PoseLandmark
    ): Float {
        val start = lineStart.position
        val end = lineEnd.position
        val lineLength = distance(lineStart, lineEnd)
        if (lineLength == 0f || end.x == start.x) return 0f

        val t = (point.position.x - start.x) / (end.x - start.x)
        val expectedY = start.y + t * (end.y - start.y)
        return (point.position.y - expectedY) / lineLength
    }
}
