package com.example.bodifyaifitness.dataclass

import com.google.firebase.firestore.PropertyName
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
    val isAISupported: Boolean = false,
    @get:PropertyName("instructions_step")
    @set:PropertyName("instructions_step")
    var instructionsStep: List<String> = emptyList()
)
