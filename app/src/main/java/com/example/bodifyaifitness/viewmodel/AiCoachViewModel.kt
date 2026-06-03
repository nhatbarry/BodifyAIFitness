package com.example.bodifyaifitness.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ChatMessage(
    val content: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

class AiCoachViewModel : ViewModel() {

    private val model by lazy {
        GenerativeModel(
            modelName = "gemini-2.5-flash",
            apiKey = com.example.bodifyaifitness.BuildConfig.GEMINI_API_KEY
        )
    }

    private val chat by lazy { model.startChat() }

    private val _messages = MutableStateFlow(
        listOf(
            ChatMessage(
                content = "Xin chào! Tôi là AI Coach của bạn 💪\nHỏi tôi bất cứ điều gì về tập luyện, dinh dưỡng, hay lịch tập nhé!",
                isUser = false
            )
        )
    )
    val messages: StateFlow<List<ChatMessage>> = _messages

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun sendMessage(content: String) {
        if (content.isBlank() || _isLoading.value) return

        _messages.value += ChatMessage(content = content.trim(), isUser = true)
        _isLoading.value = true

        viewModelScope.launch {
            try {
                val response = chat.sendMessage(content.trim())
                val reply = response.text ?: "Xin lỗi, tôi không có phản hồi cho câu hỏi này."
                _messages.value += ChatMessage(content = reply, isUser = false)
            } catch (e: Exception) {
                _messages.value += ChatMessage(
                    content = "Lỗi: ${e.message}",
                    isUser = false
                )
            } finally {
                _isLoading.value = false
            }
        }
    }
}
