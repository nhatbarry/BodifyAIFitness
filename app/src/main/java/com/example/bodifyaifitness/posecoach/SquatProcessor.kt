package com.example.bodifyaifitness.posecoach

import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark

/**
 * Squat rep counter driven by the Hip-Knee-Ankle angle.
 * Stand (angle > 160°) -> Bottom (angle < 90°) -> Stand = 1 rep.
 */
class SquatProcessor : ExerciseProcessor(downThreshold = 90.0, upThreshold = 160.0) {

    override val exerciseName = "Squat"

    // True until the hip is proven to reach at-or-below knee height during the current
    // rep attempt; reset every time the user is back to a tall stand.
    private var insufficientDepthByHipHeight = true

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

        val angle = PoseAngleUtils.angleOf(hip, knee, ankle)
        when {
            angle > 160.0 -> insufficientDepthByHipHeight = true // standing tall, reset for next rep
            angle < 110.0 && hip.position.y >= knee.position.y -> insufficientDepthByHipHeight = false
        }
        return angle
    }

    override fun checkForm(pose: Pose, angle: Double, stage: RepStage): FormFeedback? {
        if (angle > 150.0) return null // standing normally, nothing to correct

        val s = side(pose)
        val shoulder = pose.getPoseLandmark(s.shoulder) ?: return null
        val hip = pose.getPoseLandmark(s.hip) ?: return null
        val knee = pose.getPoseLandmark(s.knee) ?: return null

        val backAngle = PoseAngleUtils.angleOf(shoulder, hip, knee)
        return if (backAngle < 45.0) {
            FormFeedback("Keep your back straight!", FeedbackSeverity.WARNING)
        } else null
    }

    override fun onRepCompleted(): FormFeedback? =
        if (insufficientDepthByHipHeight) FormFeedback("Go deeper!", FeedbackSeverity.WARNING) else null

    override fun onShallowRep(): FormFeedback =
        FormFeedback("Go deeper!", FeedbackSeverity.WARNING)
}
