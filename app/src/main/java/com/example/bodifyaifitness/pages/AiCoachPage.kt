package com.example.bodifyaifitness.pages

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bodifyaifitness.R
import com.example.bodifyaifitness.dataclass.ChatSession
import com.example.bodifyaifitness.ui.theme.GymOrange
import com.example.bodifyaifitness.ui.theme.GymSurfaceBg
import com.example.bodifyaifitness.ui.theme.TextMuted
import com.example.bodifyaifitness.ui.theme.TextWhite
import com.example.bodifyaifitness.viewmodel.AiCoachViewModel
import com.example.bodifyaifitness.viewmodel.ChatMessage
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiCoachPage(
    modifier: Modifier = Modifier,
    outerBottomPadding: Dp = 0.dp,
    viewModel: AiCoachViewModel = viewModel()
) {
    val messages          by viewModel.messages.collectAsState()
    val isLoading         by viewModel.isLoading.collectAsState()
    val sessions          by viewModel.sessions.collectAsState()
    val isLoadingSessions by viewModel.isLoadingSessions.collectAsState()

    var inputText = remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // ── Khởi tạo userId từ FirebaseAuth ──────────────────────────────────────
    val currentUser = FirebaseAuth.getInstance().currentUser
    LaunchedEffect(currentUser?.uid) {
        currentUser?.uid?.let { viewModel.init(it) }
    }

    // ── Cuộn xuống tin nhắn mới nhất ─────────────────────────────────────────
    LaunchedEffect(messages.size, isLoading) {
        listState.animateScrollToItem(0)
    }

    // ── Bottom Sheet state ────────────────────────────────────────────────────
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope      = rememberCoroutineScope()
    var showSheet  by remember { mutableStateOf(false) }

    // ── Keyboard offset ───────────────────────────────────────────────────────
    val density      = LocalDensity.current
    val imeBottom    = WindowInsets.ime.getBottom(density)
    val navBarBottom = WindowInsets.navigationBars.getBottom(density)
    val keyboardOffset = with(density) {
        maxOf(0.dp, imeBottom.toDp() + navBarBottom.toDp() - outerBottomPadding)
    }

    // ── Main Scaffold ─────────────────────────────────────────────────────────
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = GymSurfaceBg,
        contentWindowInsets = WindowInsets(0),
        topBar = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(Color(0xFF1A1A2E), GymSurfaceBg)))
                    .padding(start = 16.dp, end = 4.dp, top = 14.dp, bottom = 14.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(GymOrange.copy(alpha = 0.15f))
                ) {
                    Icon(Icons.Default.SmartToy, null, tint = GymOrange, modifier = Modifier.size(22.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.title_ai_coach),
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(stringResource(R.string.subtitle_ai_coach), color = TextMuted, fontSize = 12.sp)
                }
                // Nút tạo cuộc trò chuyện mới
                IconButton(
                    onClick = { viewModel.startNewSession() },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.content_desc_new_chat),
                        tint = TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
                // Nút xem lịch sử
                IconButton(
                    onClick = {
                        viewModel.loadSessions()
                        showSheet = true
                    },
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = stringResource(R.string.content_desc_history),
                        tint = GymOrange,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        },
        bottomBar = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F0F1E))
                    .padding(bottom = keyboardOffset)
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                OutlinedTextField(
                    value = inputText.value,
                    onValueChange = { inputText.value = it },
                    placeholder = {
                        Text(stringResource(R.string.placeholder_ai_chat), color = TextMuted, fontSize = 14.sp)
                    },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Send
                    ),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (inputText.value.isNotBlank() && !isLoading) {
                                viewModel.sendMessage(inputText.value)
                                inputText.value = ""
                            }
                        }
                    ),
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GymOrange,
                        unfocusedBorderColor = Color(0xFF2A2A3E),
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        cursorColor = GymOrange,
                        focusedContainerColor = Color(0xFF12121F),
                        unfocusedContainerColor = Color(0xFF12121F)
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (inputText.value.isNotBlank() && !isLoading) {
                            viewModel.sendMessage(inputText.value)
                            inputText.value = ""
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (inputText.value.isNotBlank()) GymOrange else Color(0xFF2A2A3E))
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = stringResource(R.string.content_desc_send),
                        tint = if (inputText.value.isNotBlank()) Color.White else TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    ) { scaffoldPadding ->
        LazyColumn(
            state = listState,
            reverseLayout = true,
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp,
                top = scaffoldPadding.calculateTopPadding() + 12.dp,
                bottom = scaffoldPadding.calculateBottomPadding() + 12.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            if (isLoading) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AiBotIcon()
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp, 16.dp, 16.dp, 16.dp))
                                .background(Color(0xFF1A1A2E))
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            CircularProgressIndicator(
                                color = GymOrange,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
            items(messages.reversed()) { message ->
                MessageBubble(message = message)
            }
        }
    }

    // ── Bottom Sheet: Lịch sử chat ────────────────────────────────────────────
    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState,
            containerColor = Color(0xFF12121F),
            tonalElevation = 0.dp
        ) {
            ChatHistorySheetContent(
                sessions = sessions,
                isLoading = isLoadingSessions,
                onSessionClick = { session ->
                    scope.launch { sheetState.hide() }.invokeOnCompletion { showSheet = false }
                    viewModel.loadSession(session)
                },
                onSessionDelete = { session -> viewModel.deleteSession(session.id) },
                onNewChat = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion { showSheet = false }
                    viewModel.startNewSession()
                }
            )
        }
    }
}

// ── Bottom Sheet Content ──────────────────────────────────────────────────────

@Composable
private fun ChatHistorySheetContent(
    sessions: List<ChatSession>,
    isLoading: Boolean,
    onSessionClick: (ChatSession) -> Unit,
    onSessionDelete: (ChatSession) -> Unit,
    onNewChat: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp)
    ) {
        // ── Header ──────────────────────────────────────────────────────────
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = null,
                tint = GymOrange,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.title_chat_history),
                color = TextWhite,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                modifier = Modifier.weight(1f)
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(GymOrange)
                    .clickable { onNewChat() }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.label_new_chat),
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(color = Color(0xFF2A2A3E))
        Spacer(modifier = Modifier.height(4.dp))

        // ── Content ──────────────────────────────────────────────────────────
        when {
            isLoading -> {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                ) {
                    CircularProgressIndicator(color = GymOrange, strokeWidth = 2.dp, modifier = Modifier.size(28.dp))
                }
            }

            sessions.isEmpty() -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1A1A2E))
                    ) {
                        Icon(Icons.Default.History, null, tint = TextMuted, modifier = Modifier.size(30.dp))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.label_no_history),
                        color = TextWhite,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.label_no_history_hint),
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                }
            }

            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(minOf(sessions.size * 76, 420).dp)
                ) {
                    items(sessions, key = { it.id }) { session ->
                        ChatSessionItem(
                            session = session,
                            onClick = { onSessionClick(session) },
                            onDelete = { onSessionDelete(session) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatSessionItem(
    session: ChatSession,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val dateLabel = remember(session.updatedAt) {
        val now  = System.currentTimeMillis()
        val diff = now - session.updatedAt
        when {
            diff < 60_000L         -> "Vừa xong"
            diff < 3_600_000L      -> "${diff / 60_000}p trước"
            diff < 86_400_000L     -> "${diff / 3_600_000}h trước"
            diff < 7 * 86_400_000L -> "${diff / 86_400_000}d trước"
            else -> SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(session.updatedAt))
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF1A1A2E))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(GymOrange.copy(alpha = 0.12f))
        ) {
            Icon(Icons.Default.SmartToy, null, tint = GymOrange, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = session.title.ifBlank { "Cuộc trò chuyện" },
                color = TextWhite,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(3.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = dateLabel, color = TextMuted, fontSize = 12.sp)
                Text(" · ", color = TextMuted, fontSize = 12.sp)
                Text(text = "${session.messages.size} tin nhắn", color = TextMuted, fontSize = 12.sp)
            }
        }
        IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = stringResource(R.string.content_desc_delete_session),
                tint = Color(0xFF8B3A3A),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

// ── Message Bubble ────────────────────────────────────────────────────────────

@Composable
private fun MessageBubble(message: ChatMessage) {
    val timeLabel = remember(message.timestamp) {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp))
    }

    if (message.isUser) {
        Column(horizontalAlignment = Alignment.End, modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .clip(RoundedCornerShape(16.dp, 4.dp, 16.dp, 16.dp))
                    .background(GymOrange)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(text = parseMarkdown(message.content), color = Color.White, fontSize = 14.sp, lineHeight = 20.sp)
            }
            Text(text = timeLabel, color = TextMuted, fontSize = 11.sp, modifier = Modifier.padding(top = 3.dp))
        }
    } else {
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AiBotIcon()
            Column {
                Box(
                    modifier = Modifier
                        .widthIn(max = 280.dp)
                        .clip(RoundedCornerShape(4.dp, 16.dp, 16.dp, 16.dp))
                        .background(Color(0xFF1A1A2E))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(text = parseMarkdown(message.content), color = TextWhite, fontSize = 14.sp, lineHeight = 20.sp)
                }
                Text(text = timeLabel, color = TextMuted, fontSize = 11.sp, modifier = Modifier.padding(top = 3.dp))
            }
        }
    }
}

@Composable
private fun AiBotIcon() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(GymOrange.copy(alpha = 0.15f))
    ) {
        Icon(Icons.Default.SmartToy, null, tint = GymOrange, modifier = Modifier.size(18.dp))
    }
}

// ── Markdown ──────────────────────────────────────────────────────────────────

private fun parseMarkdown(text: String) = buildAnnotatedString {
    val lines = text.split("\n")
    lines.forEachIndexed { i, rawLine ->
        if (i > 0) append("\n")
        val line = when {
            rawLine.startsWith("* ") -> "• " + rawLine.drop(2)
            rawLine.startsWith("- ") -> "• " + rawLine.drop(2)
            else -> rawLine
        }
        val parts = line.split("**")
        parts.forEachIndexed { idx, part ->
            if (idx % 2 == 1) {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(part) }
            } else {
                append(part)
            }
        }
    }
}
