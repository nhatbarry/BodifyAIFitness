package com.example.bodifyaifitness.posecoach

import com.google.mlkit.vision.pose.Pose

/** Coarse rep phase driven by the exercise's primary joint angle. */
enum class RepStage { UP, DOWN }

enum class FeedbackSeverity { INFO, WARNING }

/** Identifies which form cue to show/speak — kept as a key (not a raw string) so the UI layer
 *  can resolve it to a localized string via Android resources, matching the app's Vietnamese/
 *  English toggle instead of always speaking English. */
enum class FeedbackKey {
    GO_DEEPER,
    BACK_STRAIGHT,
    RAISE_HIPS,
    LOWER_HIPS,
    LOWER_FURTHER
}

data class FormFeedback(val key: FeedbackKey, val severity: FeedbackSeverity)

data class ProcessorResult(
    val repCount: Int,
    val stage: RepStage,
    val primaryAngle: Double,
    /** Non-null only on the frame a new cue should be spoken (already cooldown-deduped). */
    val feedback: FormFeedback?
)

/**
 * Angle-based rep counter + form coach, shared by every exercise.
 *
 * Subclasses only supply the primary joint angle that drives the Up/Down state machine
 * and any extra real-time form checks — hysteresis, shallow-rep detection and feedback
 * cooldown all live here so adding a new exercise is just a new small subclass.
 *
 * State machine: UP (angle > [upThreshold]) -> DOWN (angle < [downThreshold]) -> UP = 1 rep.
 * If the user bounces back to UP without ever crossing [downThreshold], no rep is counted
 * and [onShallowRep] fires instead.
 */
abstract class ExerciseProcessor(
    protected val downThreshold: Double,
    protected val upThreshold: Double,
    private val feedbackCooldownMs: Long = 2500L,
    private val debounceFrames: Int = 2
) {
    abstract val exerciseName: String

    private val attemptThreshold = (downThreshold + upThreshold) / 2.0

    private var stage = RepStage.UP
    private var repCount = 0
    private var minAngleThisRep = 180.0
    private var hasDippedThisRep = false
    private val lastSpokenAt = HashMap<FeedbackKey, Long>()

    // Consecutive-frame confirmation: a stage transition only fires once the raw angle has
    // been past the threshold for [debounceFrames] frames in a row. This rejects a single
    // noisy/off-angle-camera frame the same way smoothing would, but — unlike an average —
    // it doesn't lag behind fast reps, since it only asks "did this keep being true", not
    // "what's the weighted history".
    private var consecutiveBelowDown = 0
    private var consecutiveAboveUp = 0

    /** Primary joint angle that drives the Up/Down state machine (e.g. knee angle for squat).
     *  Return null when the landmarks needed aren't reliably visible this frame — the frame
     *  is then skipped entirely rather than fed a noisy angle into the state machine. */
    protected abstract fun primaryAngle(pose: Pose): Double?

    /** Real-time form check, called every frame regardless of stage. Return null for "no issue". */
    protected abstract fun checkForm(pose: Pose, angle: Double, stage: RepStage): FormFeedback?

    /** Called right after a full DOWN -> UP rep is counted. Return extra depth/quality feedback. */
    protected abstract fun onRepCompleted(): FormFeedback?

    /** Called when the user returns to UP after dipping but never reaching [downThreshold]. */
    protected abstract fun onShallowRep(): FormFeedback?

    fun process(pose: Pose): ProcessorResult? {
        val angle = primaryAngle(pose) ?: return null
        minAngleThisRep = minOf(minAngleThisRep, angle)

        consecutiveBelowDown = if (angle < downThreshold) consecutiveBelowDown + 1 else 0
        consecutiveAboveUp = if (angle > upThreshold) consecutiveAboveUp + 1 else 0

        var feedback: FormFeedback? = null

        when (stage) {
            RepStage.UP -> when {
                consecutiveBelowDown >= debounceFrames -> {
                    stage = RepStage.DOWN
                    hasDippedThisRep = true
                }
                angle < attemptThreshold -> hasDippedThisRep = true
            }
            RepStage.DOWN -> {
                if (consecutiveAboveUp >= debounceFrames) {
                    stage = RepStage.UP
                    repCount++
                    feedback = emit(onRepCompleted())
                    minAngleThisRep = 180.0
                    hasDippedThisRep = false
                }
            }
        }

        if (feedback == null && stage == RepStage.UP && hasDippedThisRep &&
            angle >= upThreshold && minAngleThisRep > downThreshold
        ) {
            feedback = emit(onShallowRep())
            hasDippedThisRep = false
            minAngleThisRep = 180.0
        }

        if (feedback == null) {
            feedback = checkForm(pose, angle, stage)?.let { emit(it) }
        }

        return ProcessorResult(repCount, stage, angle, feedback)
    }

    /** De-dupes a cue so the same one isn't re-spoken more often than [feedbackCooldownMs]. */
    private fun emit(feedback: FormFeedback?): FormFeedback? {
        feedback ?: return null
        val now = System.currentTimeMillis()
        val last = lastSpokenAt[feedback.key] ?: 0L
        if (now - last < feedbackCooldownMs) return null
        lastSpokenAt[feedback.key] = now
        return feedback
    }

    fun reset() {
        stage = RepStage.UP
        repCount = 0
        minAngleThisRep = 180.0
        hasDippedThisRep = false
        consecutiveBelowDown = 0
        consecutiveAboveUp = 0
        lastSpokenAt.clear()
    }
}
