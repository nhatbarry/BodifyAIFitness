package com.example.bodifyaifitness.posecoach

import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark

/**
 * Push-up rep counter driven by the Shoulder-Elbow-Wrist angle, with a separate
 * Shoulder-Hip-Ankle check for body alignment (sagging vs. piking).
 * High plank (angle > 160°) -> Bottom (angle < 90°) -> High plank = 1 rep.
 */
class PushUpProcessor : ExerciseProcessor(downThreshold = 90.0, upThreshold = 160.0) {

    override val exerciseName = "Push-up"

    private data class Side(val shoulder: Int, val elbow: Int, val wrist: Int, val hip: Int, val ankle: Int)
    private val leftSide = Side(
        PoseLandmark.LEFT_SHOULDER, PoseLandmark.LEFT_ELBOW, PoseLandmark.LEFT_WRIST,
        PoseLandmark.LEFT_HIP, PoseLandmark.LEFT_ANKLE
    )
    private val rightSide = Side(
        PoseLandmark.RIGHT_SHOULDER, PoseLandmark.RIGHT_ELBOW, PoseLandmark.RIGHT_WRIST,
        PoseLandmark.RIGHT_HIP, PoseLandmark.RIGHT_ANKLE
    )

    /** Picks whichever body side ML Kit is more confident about this frame. */
    private fun side(pose: Pose): Side {
        fun score(s: Side) = listOf(s.shoulder, s.elbow, s.wrist, s.hip, s.ankle)
            .sumOf { (pose.getPoseLandmark(it)?.inFrameLikelihood ?: 0f).toDouble() }
        return if (score(rightSide) > score(leftSide)) rightSide else leftSide
    }

    override fun primaryAngle(pose: Pose): Double? {
        val s = side(pose)
        val shoulder = pose.getPoseLandmark(s.shoulder) ?: return null
        val elbow = pose.getPoseLandmark(s.elbow) ?: return null
        val wrist = pose.getPoseLandmark(s.wrist) ?: return null
        return PoseAngleUtils.angleOf(shoulder, elbow, wrist)
    }

    override fun checkForm(pose: Pose, angle: Double, stage: RepStage): FormFeedback? {
        val s = side(pose)
        val shoulder = pose.getPoseLandmark(s.shoulder) ?: return null
        val hip = pose.getPoseLandmark(s.hip) ?: return null
        val ankle = pose.getPoseLandmark(s.ankle) ?: return null

        val alignmentAngle = PoseAngleUtils.angleOf(shoulder, hip, ankle)
        if (alignmentAngle >= 160.0) return null // body is a straight line, nothing to say

        // Ratio of how far the hip sits from the shoulder-ankle line, normalized by body
        // length: positive = sagging towards the floor, negative = piking up.
        val deviationRatio = PoseAngleUtils.normalizedVerticalDeviationFromLine(shoulder, hip, ankle)
        return when {
            deviationRatio > SAG_RATIO -> FormFeedback("Keep your core tight, raise your hips!", FeedbackSeverity.WARNING)
            deviationRatio < -PIKE_RATIO -> FormFeedback("Lower your hips!", FeedbackSeverity.WARNING)
            else -> null
        }
    }

    override fun onRepCompleted(): FormFeedback? = null

    override fun onShallowRep(): FormFeedback =
        FormFeedback("Lower down further, chest closer to the ground!", FeedbackSeverity.WARNING)

    private companion object {
        // Tunable starting points; re-check against real recordings before shipping.
        const val SAG_RATIO = 0.08f
        const val PIKE_RATIO = 0.08f
    }
}
