package com.example.yoloapp.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavHostController
import com.example.yoloapp.data.ChatData
import com.example.yoloapp.ui.components.BotMessage
import com.example.yoloapp.ui.components.MainScaffold
import com.example.yoloapp.ui.components.StartAChatNow
import com.example.yoloapp.ui.components.StartingBotMessage
import com.example.yoloapp.ui.components.UserMessage
import com.example.yoloapp.ui.model.ChatMessage
import com.example.yoloapp.utils.AuthManager
import com.example.yoloapp.utils.FireStoreManager
import com.example.yoloapp.utils.getGeneratedImageUrl
import com.example.yoloapp.utils.normalizeText
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch


@Composable
fun GeneratorScreen(
    navController: NavHostController,
    authManager: AuthManager,
    fireStoreManager: FireStoreManager,
    chatId: String? = null
) {
    var messages by remember { mutableStateOf(listOf<ChatMessage>()) }
    var textState by remember { mutableStateOf(TextFieldValue("")) }
    var messageId by remember { mutableIntStateOf(0) }
    var isLoading by remember { mutableStateOf(false) }
    val email = authManager.getCurrentUser().getOrNull()?.email
    val focusRequester = remember { FocusRequester() }
    var newChatId by remember { mutableStateOf<String?>(null) }
    var isKeyboardOpen by rememberSaveable  { mutableStateOf(false) }

    val view = LocalView.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    DisposableEffect(view) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
            val isVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            isKeyboardOpen = isVisible
            insets
        }
        onDispose { }
    }

    LaunchedEffect(chatId) {
        scope.launch {
            if (chatId != null) {
                val chatData = fireStoreManager.getChatById(chatId)
                if (chatData != null) {
                    messages = chatData.messages
                    messageId = (messages.maxOfOrNull { it.id } ?: -1) + 1
                }
            }
        }
    }

    MainScaffold(
        navController = navController,
        authManager = authManager,
        fireStoreManager = fireStoreManager
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            if (chatId != null || isLoading || messages.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    reverseLayout = true
                ) {
                    items(messages.reversed()) { message ->
                        if (message.id % 2 == 0) {
                            message.text?.let { UserMessage(text = it) }
                        } else {
                            message.text?.let {
                                BotMessage(
                                    imageUrl = if (it.startsWith("http")) it else null,
                                    isLoading = it.isEmpty(),
                                    hasError = it.isBlank()
                                )
                            }
                        }
                    }
                }
            } else {
                if (isKeyboardOpen) {
                    StartingBotMessage(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                } else {
                    StartAChatNow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        onFocusRequestClick = {
                            focusRequester.requestFocus()
                            keyboardController?.show()
                        }
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .focusRequester(focusRequester),
                horizontalArrangement = Arrangement.End
            ) {


                OutlinedTextField(
                    value = textState,
                    onValueChange = { textState = it },
                    placeholder = { Text("Type a message...") },
                    modifier = Modifier.weight(1f),
                    maxLines = 3,

                    keyboardActions = KeyboardActions(
                        onDone = {
                            keyboardController?.hide()
                            focusRequester.freeFocus()
                        }
                    )

                )

                IconButton(
                    onClick = {
                        keyboardController?.hide()
                        focusRequester.freeFocus()
                        val userText = textState.text.trim()
                        if (userText.isNotEmpty()) {
                            isLoading = true
                            messages = messages + ChatMessage(
                                id = messageId++,
                                text = userText
                            )

                            val botMessageId = messageId++
                            messages = messages + ChatMessage(
                                id = botMessageId,
                                text = ""
                            )

                            textState = TextFieldValue("")

                            scope.launch {
                                try {
                                    val botResponse = getGeneratedImageUrl(userText, fireStoreManager)
                                    messages = messages.map { message ->
                                        if (message.id == botMessageId) {
                                            message.copy(text = botResponse)
                                        } else {
                                            message
                                        }
                                    }
                                    if (chatId != null || newChatId != null) {
                                        if (chatId != null) {
                                            fireStoreManager.updateChatMessages(chatId, messages)
                                        }
                                        if (newChatId != null) {
                                            fireStoreManager.updateChatMessages(
                                                newChatId!!,
                                                messages
                                            )
                                        }
                                    } else {
                                        val newChat = ChatData(
                                            email = email ?: "",
                                            title = normalizeText(userText),
                                            createdAt = Timestamp.now(),
                                            lastModifiedAt = Timestamp.now(),
                                            messages = messages
                                        )
                                        val result = fireStoreManager.saveChat(newChat)
                                        result.onSuccess { generatedNewChat ->
                                            newChatId = generatedNewChat
                                        }.onFailure { e ->
                                            Toast.makeText(context, e.message, Toast.LENGTH_SHORT)
                                                .show()
                                        }

                                    }
                                } catch (e: Exception) {
                                    messages = messages.map { message ->
                                        if (message.id == botMessageId) {
                                            message.copy(text = e.message)
                                        } else {
                                            message
                                        }
                                    }
                                } finally {
                                    isLoading = false
                                }
                            }
                        }
                    },
                    enabled = textState.text.isNotEmpty() && !isLoading,
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(if (textState.text.isNotEmpty() && !isLoading) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            tint = if (textState.text.isNotEmpty() && !isLoading) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}


