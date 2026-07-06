package com.example.bodifyaifitness.dataclass

data class ChatMessageData(
    val id: String = "",
    val content: String = "",
    val isUser: Boolean = false,
    val timestamp: Long = 0L
)

data class ChatSession(
    val id: String = "",
    val title: String = "",
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val messages: List<ChatMessageData> = emptyList()
)
