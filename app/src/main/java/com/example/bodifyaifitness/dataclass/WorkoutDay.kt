package com.example.bodifyaifitness.dataclass

data class WorkoutDay(
    val date: Long = 0L,
    val exerciseIds: List<String> = emptyList()
)
