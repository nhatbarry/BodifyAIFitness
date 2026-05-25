package com.example.bodifyaifitness.dataclass

data class Exercise(
    val id: String = "",
    val name: String = "",
    val category: String = "",
    val target: String = "",
    val muscleGroup: List<String> = emptyList(),
    val equipment: List<String> = emptyList(),
    val instruction: String = "",
    val thumbnail: String = "",
    val gif: String = "",
    val isAISupported: Boolean = false
)
