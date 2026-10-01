package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.HermesViewModel
import com.example.ui.TelegramConnectionStatus
import com.example.ui.theme.HermesCyan
import com.example.ui.theme.HermesDarkBackground
import com.example.ui.theme.HermesDarkCardBorder
import com.example.ui.theme.HermesDarkSurface
import com.example.ui.theme.HermesDarkSurfaceVariant
import com.example.ui.theme.HermesError
import com.example.ui.theme.HermesGold
import com.example.ui.theme.HermesSuccess
import com.example.ui.theme.HermesTextPrimary
import com.example.ui.theme.HermesTextSecondary
import com.example.ui.theme.HermesWarning

@Composable
fun HermesBridgeScreen(
    viewModel: HermesViewModel,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsState()
    val telegramStatus by viewModel.telegramStatus.collectAsState()
    val botName by viewModel.telegramBotName.collectAsState()

    var tokenText by remember(settings) { mutableStateOf(settings?.telegramBotToken ?: "") }
    var chatIdText by remember(settings) { mutableStateOf(settings?.telegramChatId ?: "") }
    var selectedMode by remember(settings) { mutableStateOf(settings?.mode ?: "HYBRID") }
    var isTokenVisible by remember { mutableStateOf(false) }

    var testMessageText by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HermesDarkBackground)
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text(
            text = "Hermes Telegram Interpreter",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = HermesTextPrimary
        )
        Text(
            text = "Bridge your voice commands & device controls with your Hermes Telegram Agent.",
            fontSize = 12.sp,
            color = HermesTextSecondary,
            modifier = Modifier.padding(top = 2.dp, bottom = 16.dp)
        )

        // Status Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = HermesDarkSurface),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, HermesDarkCardBorder, RoundedCornerShape(16.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = when (telegramStatus) {
                        TelegramConnectionStatus.CONNECTED -> HermesSuccess.copy(alpha = 0.2f)
                        TelegramConnectionStatus.CHECKING -> HermesWarning.copy(alpha = 0.2f)
                        TelegramConnectionStatus.FAILED -> HermesError.copy(alpha = 0.2f)
                        else -> Color.White.copy(alpha = 0.05f)
                    },
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = when (telegramStatus) {
                                TelegramConnectionStatus.CONNECTED -> Icons.Default.CloudDone
                                TelegramConnectionStatus.CHECKING -> Icons.Default.Refresh
                                TelegramConnectionStatus.FAILED -> Icons.Default.ErrorOutline
                                else -> Icons.Default.Info
                            },
                            contentDescription = null,
                            tint = when (telegramStatus) {
                                TelegramConnectionStatus.CONNECTED -> HermesSuccess
                                TelegramConnectionStatus.CHECKING -> HermesWarning
                                TelegramConnectionStatus.FAILED -> HermesError
                                else -> HermesTextSecondary
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = when (telegramStatus) {
                            TelegramConnectionStatus.CONNECTED -> "Connected to Hermes Agent"
                            TelegramConnectionStatus.CHECKING -> "Verifying Telegram Bot..."
                            TelegramConnectionStatus.FAILED -> "Connection Failed"
                            else -> "Telegram Bridge Not Linked"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = HermesTextPrimary
                    )
                    Text(
                        text = if (botName.isNotBlank()) "Bot: @$botName" else "Provide bot token and chat ID below",
                        fontSize = 11.sp,
                        color = HermesTextSecondary
                    )
                }

                OutlinedButton(
                    onClick = { viewModel.testTelegramConnection(tokenText) },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("test_telegram_connection_button")
                ) {
                    Text("Ping", fontSize = 12.sp, color = HermesCyan)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Operating Mode Selector
        Text(
            text = "Interpreter Mode",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = HermesGold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val modes = listOf("HYBRID", "HERMES_RELAY", "LOCAL_GEMINI")
            modes.forEach { mode ->
                FilterChip(
                    selected = selectedMode == mode,
                    onClick = {
                        selectedMode = mode
                        viewModel.updateSettings(
                            botToken = tokenText,
                            chatId = chatIdText,
                            mode = mode,
                            volumeTrigger = settings?.volumeKeyTriggerEnabled ?: true,
                            autoSpeak = settings?.autoSpeakResponses ?: true,
                            pitch = settings?.speechPitch ?: 1.0f,
                            rate = settings?.speechRate ?: 1.05f,
                            customApiKey = settings?.customGeminiApiKey ?: "",
                            selectedModel = settings?.selectedModel ?: "gemini-2.5-flash-native-audio-preview-12-2025",
                            extendedThinking = settings?.extendedThinkingEnabled ?: true
                        )
                    },
                    label = {
                        Text(
                            text = when (mode) {
                                "HYBRID" -> "Hybrid AI"
                                "HERMES_RELAY" -> "Hermes Direct"
                                else -> "Local Only"
                            },
                            fontSize = 11.sp
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = HermesCyan.copy(alpha = 0.2f),
                        selectedLabelColor = HermesCyan,
                        containerColor = HermesDarkSurface,
                        labelColor = HermesTextSecondary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selectedMode == mode,
                        borderColor = if (selectedMode == mode) HermesCyan else HermesDarkCardBorder
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("mode_chip_$mode")
                )
            }
        }

        Text(
            text = when (selectedMode) {
                "HYBRID" -> "Hybrid: Gemini parses natural language, manages device tasks, and relays agent queries to Hermes on Telegram."
                "HERMES_RELAY" -> "Hermes Direct: Every voice command is sent directly to your Hermes Telegram bot."
                else -> "Local Only: Acts purely as your on-device Gemini assistant without Telegram relay."
            },
            fontSize = 11.sp,
            color = HermesTextSecondary,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )

        // Credentials Configuration
        Text(
            text = "Bot Credentials",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = HermesGold
        )
        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
            value = tokenText,
            onValueChange = { tokenText = it },
            label = { Text("Telegram Bot Token", fontSize = 12.sp) },
            placeholder = { Text("123456789:ABCDefGhIJKlmNoPQRsTUVwxyZ") },
            singleLine = true,
            visualTransformation = if (isTokenVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { isTokenVisible = !isTokenVisible }) {
                    Icon(
                        imageVector = if (isTokenVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = "Toggle token visibility",
                        tint = HermesTextSecondary
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = HermesCyan,
                unfocusedBorderColor = HermesDarkCardBorder,
                focusedTextColor = HermesTextPrimary,
                unfocusedTextColor = HermesTextPrimary,
                focusedLabelColor = HermesCyan,
                unfocusedLabelColor = HermesTextSecondary
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("telegram_token_input")
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = chatIdText,
            onValueChange = { chatIdText = it },
            label = { Text("Telegram Chat ID / Channel ID", fontSize = 12.sp) },
            placeholder = { Text("e.g. 987654321 or -1001234567890") },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = HermesCyan,
                unfocusedBorderColor = HermesDarkCardBorder,
                focusedTextColor = HermesTextPrimary,
                unfocusedTextColor = HermesTextPrimary,
                focusedLabelColor = HermesCyan,
                unfocusedLabelColor = HermesTextSecondary
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("telegram_chat_id_input")
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                viewModel.updateSettings(
                    botToken = tokenText,
                    chatId = chatIdText,
                    mode = selectedMode,
                    volumeTrigger = settings?.volumeKeyTriggerEnabled ?: true,
                    autoSpeak = settings?.autoSpeakResponses ?: true,
                    pitch = settings?.speechPitch ?: 1.0f,
                    rate = settings?.speechRate ?: 1.05f,
                    customApiKey = settings?.customGeminiApiKey ?: "",
                    selectedModel = settings?.selectedModel ?: "gemini-2.5-flash-native-audio-preview-12-2025",
                    extendedThinking = settings?.extendedThinkingEnabled ?: true
                )
            },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = HermesCyan),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("save_hermes_credentials_button")
        ) {
            Text("Save Credentials & Connect", color = Color.Black, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Hermes Agent Quick Direct Test / Interpreter
        Text(
            text = "Hermes Quick Agent Commands",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = HermesGold
        )
        Spacer(modifier = Modifier.height(6.dp))

        val agentCommands = listOf("/status", "/tasks", "/memory", "/ping", "/help")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            agentCommands.forEach { cmd ->
                OutlinedButton(
                    onClick = {
                        testMessageText = cmd
                        viewModel.handleUserInput("Relay to Hermes agent: $cmd")
                    },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = cmd,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = HermesGold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = testMessageText,
                onValueChange = { testMessageText = it },
                placeholder = { Text("Send raw text/command to Hermes...", fontSize = 12.sp) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = HermesGold,
                    unfocusedBorderColor = HermesDarkCardBorder,
                    focusedTextColor = HermesTextPrimary,
                    unfocusedTextColor = HermesTextPrimary
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("hermes_direct_command_input")
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (testMessageText.isNotBlank()) {
                        viewModel.handleUserInput("Relay to Hermes agent: $testMessageText")
                        testMessageText = ""
                    }
                },
                modifier = Modifier
                    .size(44.dp)
                    .background(HermesGold, RoundedCornerShape(12.dp))
                    .testTag("hermes_direct_send_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send to Hermes",
                    tint = Color.Black,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
