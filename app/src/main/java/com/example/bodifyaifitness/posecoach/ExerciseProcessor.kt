package com.example.bodifyaifitness.posecoach

import com.google.mlkit.vision.pose.Pose

/** Coarse rep phase driven by the exercise's primary joint angle. */
enum class RepStage { UP, DOWN }

enum class FeedbackSeverity { INFO, WARNING }

data class FormFeedback(val message: String, val severity: FeedbackSeverity)

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
    private val downThreshold: Double,
    private val upThreshold: Double,
    private val feedbackCooldownMs: Long = 2500L
) {
    abstract val exerciseName: String

    private val attemptThreshold = (downThreshold + upThreshold) / 2.0

    private var stage = RepStage.UP
    private var repCount = 0
    private var minAngleThisRep = 180.0
    private var hasDippedThisRep = false
    private val lastSpokenAt = HashMap<String, Long>()

    /** Primary joint angle that drives the Up/Down state machine (e.g. knee angle for squat). */
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

        var feedback: FormFeedback? = null

        when (stage) {
            RepStage.UP -> when {
                angle < downThreshold -> {
                    stage = RepStage.DOWN
                    hasDippedThisRep = true
                }
                angle < attemptThreshold -> hasDippedThisRep = true
            }
            RepStage.DOWN -> {
                if (angle > upThreshold) {
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

    /** De-dupes a message so the same cue isn't re-spoken more often than [feedbackCooldownMs]. */
    private fun emit(feedback: FormFeedback?): FormFeedback? {
        feedback ?: return null
        val now = System.currentTimeMillis()
        val last = lastSpokenAt[feedback.message] ?: 0L
        if (now - last < feedbackCooldownMs) return null
        lastSpokenAt[feedback.message] = now
        return feedback
    }

    fun reset() {
        stage = RepStage.UP
        repCount = 0
        minAngleThisRep = 180.0
        hasDippedThisRep = false
        lastSpokenAt.clear()
    }
}
