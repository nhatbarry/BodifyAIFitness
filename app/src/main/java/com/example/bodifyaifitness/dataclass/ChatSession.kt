package com.example.bodifyaifitness.dataclass

import com.google.firebase.firestore.PropertyName

data class ChatMessageData(
    val id: String = "",
    val content: String = "",
    // Firestore's Android SDK strips the "is" prefix from boolean getters when it infers
    // the field name via reflection, so this has always been persisted as "user", not
    // "isUser" — pin it explicitly so reads match what's already saved (see Exercise.kt's
    // isAISupported for the same issue). Without this, toObject<ChatSession>() can't find
    // a matching field and silently defaults every message to isUser = false.
    @get:PropertyName("user")
    @set:PropertyName("user")
    var isUser: Boolean = false,
    val timestamp: Long = 0L
)

data class ChatSession(
    val id: String = "",
    val title: String = "",
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val messages: List<ChatMessageData> = emptyList()
)
