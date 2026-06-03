package com.example.bodifyaifitness.dataclass

import com.google.firebase.firestore.PropertyName

data class Schedule(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val days: List<WorkoutDay> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    // @PropertyName forces Firebase to use "isActive" as the Firestore field name.
    // Without it, Kotlin's isXxx() getter maps to JavaBean field "active" (not "isActive"),
    // causing batch.update("isActive", ...) to write a different field than toObject() reads.
    @get:PropertyName("isActive")
    @set:PropertyName("isActive")
    var isActive: Boolean = false
)
