package com.example

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatbotOverlay(
    onDismiss: () -> Unit,
    contextData: String,
    language: String
) {
    val viewModel: ChatbotViewModel = viewModel()
    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.statusBars).windowInsetsPadding(WindowInsets.navigationBars)) {
                // Header
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Filled.Close, contentDescription = "Close")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Outlined.SmartToy, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Ask KisanMitra",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { viewModel.clearChat() }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Clear Chat")
                        }
                    }
                }

                // Chat Area
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (messages.isEmpty()) {
                            item {
                                InitialGreeting(language = language) { suggestedText ->
                                    viewModel.sendMessage(suggestedText, contextData, language)
                                }
                            }
                        }

                        items(messages) { message ->
                            ChatBubble(message = message)
                        }

                        if (isLoading) {
                            item {
                                LoadingBubble()
                            }
                        }

                        if (error != null) {
                            item {
                                Text(
                                    text = error ?: "",
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }

                // Input Area
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .navigationBarsPadding(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            placeholder = { Text("Ask anything...") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(24.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent
                            ),
                            maxLines = 4
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(if (inputText.isNotBlank() && !isLoading) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable(enabled = inputText.isNotBlank() && !isLoading) {
                                    viewModel.sendMessage(inputText, contextData, language)
                                    inputText = ""
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.Send,
                                contentDescription = "Send",
                                tint = if (inputText.isNotBlank() && !isLoading) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val isUser = message.role == "user"
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.SmartToy, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .background(
                    if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    )
                )
                .padding(16.dp)
        ) {
            Text(
                text = message.text,
                color = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
fun LoadingBubble() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.SmartToy, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            "KisanMitra is thinking...",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun InitialGreeting(language: String, onSuggestionClick: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.SmartToy, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 16.dp)
                    )
                    .padding(16.dp)
            ) {
                val greeting = when(language) {
                    "hi" -> "नमस्ते! मैं किसानमित्र हूँ। आप मुझसे फसल, मंडी की कीमतों और खेती के बारे में कुछ भी पूछ सकते हैं।"
                    "mr" -> "नमस्कार! मी किसानमित्र आहे. तुम्ही मला पीक, बाजारभाव आणि शेतीबद्दल काहीही विचारू शकता."
                    "kn" -> "ನಮಸ್ಕಾರ! ನಾನು ಕಿಸಾನ್‌ಮಿತ್ರ. ಬೆಳೆಗಳು, ಮಾರುಕಟ್ಟೆ ಬೆಲೆಗಳು ಮತ್ತು ಕೃಷಿಯ ಬಗ್ಗೆ ನೀವು ನನ್ನನ್ನು ಏನು ಬೇಕಾದರೂ ಕೇಳಬಹುದು."
                    else -> "Namaste! I am KisanMitra. You can ask me anything about crops, mandi prices, selling, and farming."
                }
                Text(
                    text = greeting,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        val suggestions = when(language) {
            "hi" -> listOf("क्या मुझे अपनी फसल अभी बेचनी चाहिए?", "कौन सी मंडी सबसे अच्छी कीमत दे रही है?", "मेरा अनुमानित लाभ क्या है?", "मैं खराब होने से कैसे रोक सकता हूँ?")
            "mr" -> listOf("मी माझे पीक आता विकावे का?", "कोणती बाजारपेठ सर्वोत्तम किंमत देत आहे?", "माझा अंदाजित नफा किती आहे?", "मी नासाडी कशी टाळू शकतो?")
            "kn" -> listOf("ನಾನು ನನ್ನ ಬೆಳೆಯನ್ನು ಈಗ ಮಾರಾಟ ಮಾಡಬೇಕೇ?", "ಯಾವ ಮಾರುಕಟ್ಟೆಯು ಉತ್ತಮ ಬೆಲೆಯನ್ನು ನೀಡುತ್ತಿದೆ?", "ನನ್ನ ಅಂದಾಜು ಲಾಭ ಎಷ್ಟು?", "ಹಾಳಾಗುವುದನ್ನು ನಾನು ಹೇಗೆ ಕಡಿಮೆ ಮಾಡಬಹುದು?")
            else -> listOf("Should I sell my crop now?", "Which mandi is offering the best price?", "What is my estimated profit?", "How can I reduce spoilage?", "How does the auction work?")
        }
        
        Text("Suggested questions:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 44.dp))
        Spacer(modifier = Modifier.height(8.dp))
        
        Column(modifier = Modifier.padding(start = 44.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            suggestions.forEach { text ->
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.clickable { onSuggestionClick(text) }
                ) {
                    Text(text, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
