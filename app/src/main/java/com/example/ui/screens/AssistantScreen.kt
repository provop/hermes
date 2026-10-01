package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Intent
import com.example.HermesPopupActivity
import com.example.ui.AssistantStatus
import com.example.ui.HermesViewModel
import com.example.ui.TelegramConnectionStatus
import com.example.ui.components.MessageBubble
import com.example.ui.components.VoiceOrbVisualizer
import com.example.ui.theme.HermesCyan
import com.example.ui.theme.HermesDarkBackground
import com.example.ui.theme.HermesDarkCardBorder
import com.example.ui.theme.HermesDarkSurface
import com.example.ui.theme.HermesGold
import com.example.ui.theme.HermesSuccess
import com.example.ui.theme.HermesTextPrimary
import com.example.ui.theme.HermesTextSecondary
import com.example.ui.theme.HermesVoiceActiveGlow
import com.example.ui.theme.HermesWarning

@Composable
fun AssistantScreen(
    viewModel: HermesViewModel,
    onRequestRecordAudioPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    val status by viewModel.status.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val isListening by viewModel.voiceManager.isListening.collectAsState()
    val isSpeaking by viewModel.voiceManager.isSpeaking.collectAsState()
    val audioRms by viewModel.voiceManager.rmsDb.collectAsState()
    val partialTranscript by viewModel.voiceManager.partialTranscript.collectAsState()
    val telegramStatus by viewModel.telegramStatus.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val context = LocalContext.current

    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Auto-scroll to latest message
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HermesDarkBackground)
    ) {
        // Status header & Hermes Telegram pill
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "HERMES AI",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = HermesCyan,
                    letterSpacing = 1.5.sp
                )
                val modelName = when {
                    settings?.selectedModel?.contains("live", ignoreCase = true) == true -> "Live 3.8"
                    settings?.selectedModel?.contains("pro", ignoreCase = true) == true -> "Pro 3.1"
                    else -> "Live"
                }
                val thinkingBadge = if (settings?.extendedThinkingEnabled != false) " [Thinking]" else ""
                Text(
                    text = "${settings?.mode ?: "HYBRID"} • $modelName$thinkingBadge",
                    fontSize = 11.sp,
                    color = HermesTextSecondary
                )
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = when (telegramStatus) {
                    TelegramConnectionStatus.CONNECTED -> HermesSuccess.copy(alpha = 0.15f)
                    TelegramConnectionStatus.CHECKING -> HermesWarning.copy(alpha = 0.15f)
                    else -> Color.White.copy(alpha = 0.08f)
                },
                modifier = Modifier.border(
                    1.dp,
                    when (telegramStatus) {
                        TelegramConnectionStatus.CONNECTED -> HermesSuccess
                        TelegramConnectionStatus.CHECKING -> HermesWarning
                        else -> HermesDarkCardBorder
                    },
                    RoundedCornerShape(16.dp)
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(
                                when (telegramStatus) {
                                    TelegramConnectionStatus.CONNECTED -> HermesSuccess
                                    TelegramConnectionStatus.CHECKING -> HermesWarning
                                    else -> HermesTextSecondary
                                },
                                CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = when (telegramStatus) {
                            TelegramConnectionStatus.CONNECTED -> "Hermes Agent Online"
                            TelegramConnectionStatus.CHECKING -> "Checking Telegram..."
                            else -> "Hermes Standalone"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = when (telegramStatus) {
                            TelegramConnectionStatus.CONNECTED -> HermesSuccess
                            TelegramConnectionStatus.CHECKING -> HermesWarning
                            else -> HermesTextSecondary
                        }
                    )
                }
            }
        }

        // Floating Overlay Quick Launch Banner
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Trigger: Floating Popup Active",
                fontSize = 11.sp,
                color = HermesCyan,
                fontWeight = FontWeight.Medium
            )

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = HermesCyan.copy(alpha = 0.15f),
                border = androidx.compose.foundation.BorderStroke(1.dp, HermesCyan.copy(alpha = 0.5f)),
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        val popupIntent = Intent(context, HermesPopupActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        }
                        context.startActivity(popupIntent)
                    }
                    .testTag("open_popup_overlay_banner_button")
            ) {
                Text(
                    text = "Launch Popup ↗",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = HermesCyan,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        // Voice Orb Section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                VoiceOrbVisualizer(
                    status = status,
                    audioAmplitude = audioRms,
                    size = 130.dp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = when {
                        isListening && partialTranscript.isNotBlank() -> "\"$partialTranscript\""
                        isListening -> "Listening... Speak your command"
                        isSpeaking -> "Speaking response..."
                        status == AssistantStatus.THINKING -> "Interpreting with Gemini & Hermes..."
                        else -> statusMessage
                    },
                    fontSize = 12.sp,
                    color = if (isListening) HermesCyan else HermesTextSecondary,
                    fontWeight = if (isListening) FontWeight.SemiBold else FontWeight.Normal,
                    modifier = Modifier.padding(horizontal = 24.dp),
                    maxLines = 2
                )
            }
        }

        // Quick suggested action pills
        val quickSuggestions = listOf(
            "👁️ Read My Screen",
            "Draft email to team",
            "Call 555-0199",
            "Turn on flashlight",
            "Open Wi-Fi settings",
            "Set timer for 10 minutes",
            "Ask Hermes Agent /status"
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            quickSuggestions.forEach { suggestion ->
                val isScreenRead = suggestion.startsWith("👁️")
                FilterChip(
                    selected = isScreenRead,
                    onClick = {
                        if (isScreenRead) {
                            viewModel.handleUserInput("Read what is on my screen right now and summarize key details and next actions.")
                        } else {
                            viewModel.handleUserInput(suggestion)
                        }
                    },
                    label = {
                        Text(
                            text = suggestion,
                            fontSize = 11.sp,
                            fontWeight = if (isScreenRead) FontWeight.Bold else FontWeight.Normal,
                            color = if (isScreenRead) HermesCyan else HermesTextPrimary
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = if (isScreenRead) HermesCyan.copy(alpha = 0.15f) else HermesDarkSurface,
                        labelColor = if (isScreenRead) HermesCyan else HermesTextPrimary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isScreenRead,
                        borderColor = if (isScreenRead) HermesCyan else HermesDarkCardBorder
                    ),
                    modifier = Modifier.testTag("suggestion_chip_${suggestion.take(8)}")
                )
            }
        }

        // Conversation history stream
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (messages.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Hermes Personal Assistant is Ready",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = HermesTextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Press the mic below, tap a quick action, or use the hardware hotkey (double tap Volume Down) to speak.",
                        fontSize = 12.sp,
                        color = HermesTextSecondary,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 4.dp)
                ) {
                    items(messages, key = { it.id }) { message ->
                        MessageBubble(
                            message = message,
                            onSpeakClick = { textToSpeak ->
                                viewModel.voiceManager.speak(textToSpeak)
                            }
                        )
                    }
                }
            }
        }

        // Active Speaking Banner if currently speaking
        AnimatedVisibility(visible = isSpeaking) {
            Surface(
                color = HermesCyan.copy(alpha = 0.12f),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, HermesCyan.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = null,
                            tint = HermesCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Hermes is speaking...",
                            fontSize = 12.sp,
                            color = HermesCyan
                        )
                    }
                    IconButton(
                        onClick = { viewModel.stopSpeaking() },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "Stop speaking",
                            tint = HermesCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Bottom Input Row (Voice Mic + Text Input + Send)
        Surface(
            color = HermesDarkSurface,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, HermesDarkCardBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Voice Listening Toggle Button
                IconButton(
                    onClick = {
                        onRequestRecordAudioPermission()
                        if (isListening) {
                            viewModel.stopListening()
                        } else {
                            viewModel.startListening()
                        }
                    },
                    modifier = Modifier
                        .size(46.dp)
                        .background(
                            if (isListening) HermesVoiceActiveGlow.copy(alpha = 0.25f) else HermesCyan.copy(alpha = 0.15f),
                            CircleShape
                        )
                        .border(
                            1.5.dp,
                            if (isListening) HermesVoiceActiveGlow else HermesCyan,
                            CircleShape
                        )
                        .testTag("voice_mic_button")
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Voice input",
                        tint = if (isListening) HermesVoiceActiveGlow else HermesCyan,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            text = if (isListening) "Listening..." else "Ask Hermes or command phone...",
                            fontSize = 13.sp,
                            color = HermesTextSecondary
                        )
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HermesCyan,
                        unfocusedBorderColor = HermesDarkCardBorder,
                        focusedTextColor = HermesTextPrimary,
                        unfocusedTextColor = HermesTextPrimary,
                        cursorColor = HermesCyan
                    ),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("command_input_field")
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            viewModel.handleUserInput(inputText)
                            inputText = ""
                        }
                    },
                    enabled = inputText.isNotBlank(),
                    modifier = Modifier
                        .size(42.dp)
                        .background(
                            if (inputText.isNotBlank()) HermesCyan else Color.White.copy(alpha = 0.05f),
                            CircleShape
                        )
                        .testTag("send_command_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = if (inputText.isNotBlank()) Color.Black else HermesTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
