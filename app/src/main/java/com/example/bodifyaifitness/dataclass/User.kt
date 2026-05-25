package com.example.bodifyaifitness.dataclass


data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val height: Float = 0f,
    val weight: Float = 0f,
    val avatarUri: String = "",
    val joinDate: Long = System.currentTimeMillis()
)
