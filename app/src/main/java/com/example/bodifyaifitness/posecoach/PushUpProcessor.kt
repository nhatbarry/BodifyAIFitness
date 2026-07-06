package com.example.bodifyaifitness.posecoach

import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark

/**
 * Push-up rep counter driven by the Shoulder-Elbow-Wrist angle, with a separate
 * Shoulder-Hip-Ankle check for body alignment (sagging vs. piking).
 * High plank (angle > 160°) -> Bottom (angle < 140°) -> High plank = 1 rep.
 *
 * downThreshold loosened well past squat's equivalent (100°) because the elbow angle is much
 * more sensitive to camera viewing angle than a squat's hip-knee-ankle angle: a squat's leg
 * swings mostly within the camera's image plane, but a push-up's arm bend is easily
 * foreshortened by anything less than a perfect side-on camera, making the measured angle
 * read straighter than the real bend. The same foreshortening is why the alignment check
 * below also needs a wider dead zone and debounce, or it fires on camera-angle noise alone.
 */
class PushUpProcessor : ExerciseProcessor(downThreshold = 140.0, upThreshold = 160.0) {

    override val exerciseName = "Push-up"

    private var consecutiveSagging = 0
    private var consecutivePiking = 0

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
        // Off-angle camera / partial occlusion makes ML Kit guess landmark positions —
        // skip the frame instead of feeding a noisy angle into the rep counter.
        if (!PoseAngleUtils.allConfident(shoulder, elbow, wrist)) return null
        return PoseAngleUtils.angleOf(shoulder, elbow, wrist)
    }

    override fun checkForm(pose: Pose, angle: Double, stage: RepStage): FormFeedback? {
        val s = side(pose)
        val shoulder = pose.getPoseLandmark(s.shoulder) ?: return null
        val hip = pose.getPoseLandmark(s.hip) ?: return null
        val ankle = pose.getPoseLandmark(s.ankle) ?: return null
        if (!PoseAngleUtils.allConfident(shoulder, hip, ankle)) return null

        val alignmentAngle = PoseAngleUtils.angleOf(shoulder, hip, ankle)
        if (alignmentAngle >= 160.0) {
            consecutiveSagging = 0
            consecutivePiking = 0
            return null // body is a straight line, nothing to say
        }

        // Ratio of how far the hip sits from the shoulder-ankle line, normalized by body
        // length: positive = sagging towards the floor, negative = piking up.
        val deviationRatio = PoseAngleUtils.normalizedVerticalDeviationFromLine(shoulder, hip, ankle)
        consecutiveSagging = if (deviationRatio > SAG_RATIO) consecutiveSagging + 1 else 0
        consecutivePiking = if (deviationRatio < -PIKE_RATIO) consecutivePiking + 1 else 0

        // Same debounce idea as rep counting: require the deviation to hold for a few frames
        // in a row before speaking up, so camera-angle jitter alone can't trigger it.
        return when {
            consecutiveSagging >= FORM_DEBOUNCE_FRAMES -> FormFeedback(FeedbackKey.RAISE_HIPS, FeedbackSeverity.WARNING)
            consecutivePiking >= FORM_DEBOUNCE_FRAMES -> FormFeedback(FeedbackKey.LOWER_HIPS, FeedbackSeverity.WARNING)
            else -> null
        }
    }

    override fun onRepCompleted(): FormFeedback? = null

    override fun onShallowRep(): FormFeedback =
        FormFeedback(FeedbackKey.LOWER_FURTHER, FeedbackSeverity.WARNING)

    private companion object {
        // Widened from 0.08 — a perfectly straight body can still measure some deviation once
        // the camera isn't a perfect side-on view, so the dead zone needs more margin than a
        // straight-on measurement would.
        const val SAG_RATIO = 0.12f
        const val PIKE_RATIO = 0.12f
        const val FORM_DEBOUNCE_FRAMES = 3
    }
}
