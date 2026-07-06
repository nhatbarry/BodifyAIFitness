package com.example.bodifyaifitness.posecoach

import com.example.bodifyaifitness.dataclass.Exercise

/**
 * Maps Exercise.aiAlgorithm (set manually in Firestore alongside isAISupported) to the
 * concrete processor that knows how to count reps / check form for it. Several exercises can
 * share the same algorithm. Returns null for an unrecognized or blank value — callers should
 * treat that as "not actually supported yet".
 */
fun exerciseProcessorFor(exercise: Exercise): ExerciseProcessor? =
    when (exercise.aiAlgorithm.trim().lowercase()) {
        "squat" -> SquatProcessor()
        "pushup", "push_up", "push-up" -> PushUpProcessor()
        else -> null
    }
