package com.example.bodifyaifitness.dataclass


data class WorkOutLog(
    val id: String = "",
    val date: Long = System.currentTimeMillis(),
    val duration: Int = 0,
    val note: String = "",
    val exercise: List<CompleteExercise> = emptyList(),
)

data class CompleteExercise(
    val exerciseId: String = "",
    val exerciseName: String = "",
    val sets: List<ExerciseSet> = emptyList()
)

data class ExerciseSet(
    val reps: Int = 0,
    val weight: Double = 0.0,
    val isAiTracked: Boolean = false,
)
