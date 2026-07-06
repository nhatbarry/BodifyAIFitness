package com.example.bodifyaifitness.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bodifyaifitness.database.FirebaseManager
import com.example.bodifyaifitness.dataclass.ChatMessageData
import com.example.bodifyaifitness.dataclass.ChatSession
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

data class ChatMessage(
    val content: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

class AiCoachViewModel : ViewModel() {

    private val db = FirebaseManager()

    private val model by lazy {
        GenerativeModel(
            modelName = "gemini-2.5-flash",
            apiKey = com.example.bodifyaifitness.BuildConfig.GEMINI_API_KEY,
            systemInstruction = content {
                text("Bạn là AI Coach thể dục chuyên nghiệp. Hãy trả lời ngắn gọn, rõ ràng bằng tiếng Việt.")
            }
        )
    }

    private var chat = model.startChat()

    // ── Messages trong session hiện tại ───────────────────────────────────────
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

    // ── Danh sách session lịch sử ─────────────────────────────────────────────
    private val _sessions = MutableStateFlow<List<ChatSession>>(emptyList())
    val sessions: StateFlow<List<ChatSession>> = _sessions

    private val _isLoadingSessions = MutableStateFlow(false)
    val isLoadingSessions: StateFlow<Boolean> = _isLoadingSessions

    // ── Session ID đang hoạt động ─────────────────────────────────────────────
    private var currentSessionId: String = UUID.randomUUID().toString()
    private var currentUserId: String = ""

    // ── Đánh dấu session hiện tại đã có tin nhắn thực sự chưa ───────────────
    private var hasRealMessage = false

    // ── Init: load sessions từ Firestore ─────────────────────────────────────
    fun init(userId: String) {
        if (currentUserId == userId) return
        currentUserId = userId
        loadSessions()
    }

    fun loadSessions() {
        if (currentUserId.isEmpty()) return
        _isLoadingSessions.value = true
        db.getChatSessions(
            userId = currentUserId,
            onSuccess = { list ->
                _sessions.value = list
                _isLoadingSessions.value = false
            },
            onFailure = { _isLoadingSessions.value = false }
        )
    }

    // ── Gửi tin nhắn ─────────────────────────────────────────────────────────
    fun sendMessage(content: String) {
        if (content.isBlank() || _isLoading.value) return

        hasRealMessage = true
        _messages.value += ChatMessage(content = content.trim(), isUser = true)
        _isLoading.value = true

        viewModelScope.launch {
            try {
                val response = chat.sendMessage(content.trim())
                val reply = response.text ?: "Xin lỗi, tôi không có phản hồi cho câu hỏi này."
                _messages.value += ChatMessage(content = reply, isUser = false)
            } catch (e: Exception) {
                _messages.value += ChatMessage(
                    content = "Lỗi kết nối: ${e.message}",
                    isUser = false
                )
            } finally {
                _isLoading.value = false
                saveCurrentSession()
            }
        }
    }

    // ── Tạo cuộc hội thoại mới ───────────────────────────────────────────────
    fun startNewSession() {
        currentSessionId = UUID.randomUUID().toString()
        hasRealMessage = false
        chat = model.startChat()
        _messages.value = listOf(
            ChatMessage(
                content = "Xin chào! Tôi là AI Coach của bạn 💪\nHỏi tôi bất cứ điều gì về tập luyện, dinh dưỡng, hay lịch tập nhé!",
                isUser = false
            )
        )
    }

    // ── Load lại 1 session cũ ────────────────────────────────────────────────
    fun loadSession(session: ChatSession) {
        currentSessionId = session.id
        hasRealMessage = true

        // Chuyển đổi sang ChatMessage để hiển thị UI
        _messages.value = buildList {
            add(ChatMessage(
                content = "Xin chào! Tôi là AI Coach của bạn 💪\nHỏi tôi bất cứ điều gì về tập luyện, dinh dưỡng, hay lịch tập nhé!",
                isUser = false
            ))
            addAll(session.messages.map { ChatMessage(it.content, it.isUser, it.timestamp) })
        }

        // Khôi phục lại chat history của Gemini để AI nhớ ngữ cảnh
        val history = session.messages.map { msg ->
            content(if (msg.isUser) "user" else "model") { text(msg.content) }
        }
        chat = model.startChat(history = history)
    }

    // ── Xóa 1 session ────────────────────────────────────────────────────────
    fun deleteSession(sessionId: String) {
        if (currentUserId.isEmpty()) return
        db.deleteChatSession(
            userId = currentUserId,
            sessionId = sessionId,
            onSuccess = {
                _sessions.value = _sessions.value.filter { it.id != sessionId }
                // Nếu đang xem session vừa xoá → tạo mới
                if (sessionId == currentSessionId) startNewSession()
            }
        )
    }

    // ── Private: lưu session hiện tại ────────────────────────────────────────
    private fun saveCurrentSession() {
        if (currentUserId.isEmpty() || !hasRealMessage) return

        val realMessages = _messages.value.drop(1) // bỏ lời chào mặc định
        if (realMessages.isEmpty()) return

        val now = System.currentTimeMillis()
        val title = realMessages
            .firstOrNull { it.isUser }?.content
            ?.take(40)
            ?.trimEnd()
            ?: "Cuộc trò chuyện mới"

        val session = ChatSession(
            id = currentSessionId,
            title = title,
            createdAt = _sessions.value.find { it.id == currentSessionId }?.createdAt ?: now,
            updatedAt = now,
            messages = realMessages.map {
                ChatMessageData(
                    id = UUID.randomUUID().toString(),
                    content = it.content,
                    isUser = it.isUser,
                    timestamp = it.timestamp
                )
            }
        )

        db.saveChatSession(
            userId = currentUserId,
            session = session,
            onSuccess = {
                // Cập nhật danh sách local
                val existing = _sessions.value.toMutableList()
                val idx = existing.indexOfFirst { it.id == session.id }
                if (idx >= 0) existing[idx] = session else existing.add(0, session)
                _sessions.value = existing.sortedByDescending { it.updatedAt }
            }
        )
    }
}
