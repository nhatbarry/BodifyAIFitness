package com.example.bodifyaifitness.posecoach

import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark

/**
 * Squat rep counter driven by the Hip-Knee-Ankle angle.
 * Stand (angle > 160°) -> Bottom (angle < 120°) -> Stand = 1 rep.
 *
 * downThreshold loosened further to 120° (was 100°, originally 90°) — a shallow/half squat
 * now counts, no need to reach full/parallel depth. The old separate "hip at-or-below knee
 * height" requirement was dropped entirely since it enforced a stricter depth than the angle
 * threshold already does, which was the opposite of what's wanted here.
 */
class SquatProcessor : ExerciseProcessor(downThreshold = 120.0, upThreshold = 160.0) {

    override val exerciseName = "Squat"

    private data class Side(val shoulder: Int, val hip: Int, val knee: Int, val ankle: Int)
    private val leftSide = Side(
        PoseLandmark.LEFT_SHOULDER, PoseLandmark.LEFT_HIP, PoseLandmark.LEFT_KNEE, PoseLandmark.LEFT_ANKLE
    )
    private val rightSide = Side(
        PoseLandmark.RIGHT_SHOULDER, PoseLandmark.RIGHT_HIP, PoseLandmark.RIGHT_KNEE, PoseLandmark.RIGHT_ANKLE
    )

    /** Picks whichever body side ML Kit is more confident about this frame. */
    private fun side(pose: Pose): Side {
        fun score(s: Side) = listOf(s.shoulder, s.hip, s.knee, s.ankle)
            .sumOf { (pose.getPoseLandmark(it)?.inFrameLikelihood ?: 0f).toDouble() }
        return if (score(rightSide) > score(leftSide)) rightSide else leftSide
    }

    override fun primaryAngle(pose: Pose): Double? {
        val s = side(pose)
        val hip = pose.getPoseLandmark(s.hip) ?: return null
        val knee = pose.getPoseLandmark(s.knee) ?: return null
        val ankle = pose.getPoseLandmark(s.ankle) ?: return null
        // Off-angle camera / partial occlusion makes ML Kit guess landmark positions —
        // skip the frame instead of feeding a noisy angle into the rep counter.
        if (!PoseAngleUtils.allConfident(hip, knee, ankle)) return null

        return PoseAngleUtils.angleOf(hip, knee, ankle)
    }

    override fun checkForm(pose: Pose, angle: Double, stage: RepStage): FormFeedback? {
        if (angle > upThreshold - 10.0) return null // standing normally, nothing to correct

        val s = side(pose)
        val shoulder = pose.getPoseLandmark(s.shoulder) ?: return null
        val hip = pose.getPoseLandmark(s.hip) ?: return null
        val knee = pose.getPoseLandmark(s.knee) ?: return null
        if (!PoseAngleUtils.allConfident(shoulder, hip, knee)) return null

        val backAngle = PoseAngleUtils.angleOf(shoulder, hip, knee)
        return if (backAngle < 45.0) {
            FormFeedback(FeedbackKey.BACK_STRAIGHT, FeedbackSeverity.WARNING)
        } else null
    }

    override fun onRepCompleted(): FormFeedback? = null

    override fun onShallowRep(): FormFeedback =
        FormFeedback(FeedbackKey.GO_DEEPER, FeedbackSeverity.WARNING)
}
