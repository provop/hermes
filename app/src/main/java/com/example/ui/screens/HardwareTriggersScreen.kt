package com.example.ui.screens

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.HermesPopupActivity
import com.example.service.TriggerEventBus
import com.example.service.TriggerSource
import com.example.ui.HermesViewModel
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
fun HardwareTriggersScreen(
    viewModel: HermesViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsState()

    var volumeTriggerEnabled by remember(settings) {
        mutableStateOf(settings?.volumeKeyTriggerEnabled ?: true)
    }
    var autoSpeakEnabled by remember(settings) {
        mutableStateOf(settings?.autoSpeakResponses ?: true)
    }
    var speechPitch by remember(settings) {
        mutableFloatStateOf(settings?.speechPitch ?: 1.0f)
    }
    var speechRate by remember(settings) {
        mutableFloatStateOf(settings?.speechRate ?: 1.05f)
    }

    var apiKeyInput by remember(settings) {
        mutableStateOf(settings?.customGeminiApiKey ?: "")
    }
    var selectedModel by remember(settings) {
        mutableStateOf(settings?.selectedModel ?: "gemini-2.5-flash-native-audio-preview-12-2025")
    }
    var extendedThinking by remember(settings) {
        mutableStateOf(settings?.extendedThinkingEnabled ?: true)
    }
    var isKeyVisible by remember { mutableStateOf(false) }
    var keyValidationMessage by remember { mutableStateOf<String?>(null) }
    var isKeyValidating by remember { mutableStateOf(false) }
    var isKeyValid by remember { mutableStateOf<Boolean?>(null) }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HermesDarkBackground)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Settings & Hardware Triggers",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = HermesTextPrimary
        )
        Text(
            text = "Configure your own Gemini API key (BYOK), physical button triggers, and voice synthesis.",
            fontSize = 12.sp,
            color = HermesTextSecondary,
            modifier = Modifier.padding(top = 2.dp)
        )

        // 0. Bring Your Own Key (BYOK) - Gemini AI
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = HermesDarkSurface),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, HermesDarkCardBorder, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = HermesGold.copy(alpha = 0.15f),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Key,
                                    contentDescription = null,
                                    tint = HermesGold,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Bring Your Own Key (BYOK)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = HermesTextPrimary
                            )
                            Text(
                                text = "Personal Gemini API Key",
                                fontSize = 11.sp,
                                color = HermesGold
                            )
                        }
                    }

                    // Status Pill
                    val isCustomKeyActive = !settings?.customGeminiApiKey.isNullOrBlank()
                    val hasDefaultKey = BuildConfig.GEMINI_API_KEY.isNotBlank() && BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY"

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = when {
                            isCustomKeyActive -> HermesSuccess.copy(alpha = 0.15f)
                            hasDefaultKey -> HermesCyan.copy(alpha = 0.15f)
                            else -> HermesWarning.copy(alpha = 0.15f)
                        }
                    ) {
                        Text(
                            text = when {
                                isCustomKeyActive -> "BYOK Active"
                                hasDefaultKey -> "Default Active"
                                else -> "Key Missing"
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                isCustomKeyActive -> HermesSuccess
                                hasDefaultKey -> HermesCyan
                                else -> HermesWarning
                            },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Supply your personal Gemini API key to power all voice intelligence, surrounding analysis, and tool calls. Keys are stored locally on your device.",
                    fontSize = 12.sp,
                    color = HermesTextSecondary,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = apiKeyInput,
                    onValueChange = {
                        apiKeyInput = it
                        isKeyValid = null
                        keyValidationMessage = null
                    },
                    label = { Text("Gemini API Key", fontSize = 12.sp) },
                    placeholder = { Text("AIzaSy...") },
                    singleLine = true,
                    visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (apiKeyInput.isNotBlank()) {
                                IconButton(onClick = {
                                    apiKeyInput = ""
                                    isKeyValid = null
                                    keyValidationMessage = null
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear key",
                                        tint = HermesTextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            IconButton(onClick = { isKeyVisible = !isKeyVisible }) {
                                Icon(
                                    imageVector = if (isKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle visibility",
                                    tint = HermesTextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
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
                        .testTag("byok_api_key_input")
                )

                // Validation feedback banner
                keyValidationMessage?.let { msg ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isKeyValid == true) HermesSuccess.copy(alpha = 0.15f) else HermesError.copy(alpha = 0.15f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isKeyValid == true) Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = if (isKeyValid == true) HermesSuccess else HermesError,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = msg,
                                fontSize = 11.sp,
                                color = if (isKeyValid == true) HermesSuccess else HermesError,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Model Selection Section
                Text(
                    text = "AI Model Engine",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = HermesGold
                )
                Spacer(modifier = Modifier.height(6.dp))

                val availableModels = listOf(
                    Triple(
                        "Gemini 3.5 Flash (Recommended)",
                        "gemini-3.5-flash",
                        "Ultra-fast voice assistant with vision & device tool calling"
                    ),
                    Triple(
                        "Gemini Live 3.8 Extended Thinking",
                        "gemini-3.1-pro-preview",
                        "Deep multi-step reasoning with thinkingConfig budget"
                    ),
                    Triple(
                        "Gemini 3.1 Flash Lite",
                        "gemini-3.1-flash-lite-preview",
                        "Lowest latency quick voice responses"
                    )
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    availableModels.forEach { (label, modelId, desc) ->
                        val isSelected = selectedModel == modelId
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) HermesCyan.copy(alpha = 0.12f) else HermesDarkSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    1.dp,
                                    if (isSelected) HermesCyan else HermesDarkCardBorder,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { selectedModel = modelId }
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { selectedModel = modelId },
                                    colors = RadioButtonDefaults.colors(selectedColor = HermesCyan)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = label,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) HermesCyan else HermesTextPrimary
                                    )
                                    Text(
                                        text = desc,
                                        fontSize = 10.sp,
                                        color = HermesTextSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Extended Thinking Toggle
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (extendedThinking) HermesGold.copy(alpha = 0.12f) else HermesDarkSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            if (extendedThinking) HermesGold.copy(alpha = 0.6f) else HermesDarkCardBorder,
                            RoundedCornerShape(10.dp)
                        )
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Extended Thinking Mode",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (extendedThinking) HermesGold else HermesTextPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (extendedThinking) HermesGold else HermesDarkCardBorder
                                ) {
                                    Text(
                                        text = if (extendedThinking) "ACTIVE" else "OFF",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.Black,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Text(
                                text = "High thinkingLevel reasoning budget for deep multi-step device commands and agent interpretations.",
                                fontSize = 10.sp,
                                color = HermesTextSecondary,
                                lineHeight = 14.sp
                            )
                        }
                        Switch(
                            checked = extendedThinking,
                            onCheckedChange = { extendedThinking = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = HermesGold,
                                uncheckedThumbColor = HermesTextSecondary,
                                uncheckedTrackColor = HermesDarkSurfaceVariant
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Test key button
                    OutlinedButton(
                        onClick = {
                            if (apiKeyInput.isNotBlank()) {
                                isKeyValidating = true
                                keyValidationMessage = null
                                viewModel.testGeminiApiKey(apiKeyInput, selectedModel) { success, msg ->
                                    isKeyValidating = false
                                    isKeyValid = success
                                    keyValidationMessage = msg
                                }
                            } else {
                                keyValidationMessage = "Enter an API key first."
                                isKeyValid = false
                            }
                        },
                        enabled = !isKeyValidating && apiKeyInput.isNotBlank(),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("test_byok_key_button")
                    ) {
                        if (isKeyValidating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                strokeWidth = 2.dp,
                                color = HermesCyan
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Testing...", fontSize = 11.sp, color = HermesCyan)
                        } else {
                            Text("Test Model & Key", fontSize = 11.sp, color = HermesCyan)
                        }
                    }

                    // Save BYOK button
                    Button(
                        onClick = {
                            viewModel.updateSettings(
                                botToken = settings?.telegramBotToken ?: "",
                                chatId = settings?.telegramChatId ?: "",
                                mode = settings?.mode ?: "HYBRID",
                                volumeTrigger = volumeTriggerEnabled,
                                autoSpeak = autoSpeakEnabled,
                                pitch = speechPitch,
                                rate = speechRate,
                                customApiKey = apiKeyInput.trim(),
                                selectedModel = selectedModel,
                                extendedThinking = extendedThinking
                            )
                            keyValidationMessage = "BYOK Key & Model saved successfully!"
                            isKeyValid = true
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = HermesCyan),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_byok_key_button")
                    ) {
                        Text("Save Config", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }

                // Revert button if custom key is currently set
                if (!settings?.customGeminiApiKey.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = {
                            apiKeyInput = ""
                            selectedModel = "gemini-3.5-flash"
                            extendedThinking = true
                            viewModel.updateSettings(
                                botToken = settings?.telegramBotToken ?: "",
                                chatId = settings?.telegramChatId ?: "",
                                mode = settings?.mode ?: "HYBRID",
                                volumeTrigger = volumeTriggerEnabled,
                                autoSpeak = autoSpeakEnabled,
                                pitch = speechPitch,
                                rate = speechRate,
                                customApiKey = "",
                                selectedModel = "gemini-3.5-flash",
                                extendedThinking = true
                            )
                            keyValidationMessage = "Reverted to default BuildConfig key & model."
                            isKeyValid = true
                        },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(
                            text = "Clear BYOK & Revert to App Default Key",
                            fontSize = 11.sp,
                            color = HermesTextSecondary
                        )
                    }
                }
            }
        }

        // Floating Assistant Popup Overlay Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = HermesDarkSurface),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, HermesCyan.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = HermesCyan.copy(alpha = 0.15f),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = null,
                                    tint = HermesCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Instant Assistant Popup Overlay",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = HermesTextPrimary
                            )
                            Text(
                                text = "Non-Intrusive Floating Resolution Sheet",
                                fontSize = 11.sp,
                                color = HermesCyan
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = HermesSuccess.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "ENABLED",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = HermesSuccess,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "When hardware triggers (Power Button long-press, corner swipe, Volume Down double-tap, or Quick Settings Tile) are invoked, Hermes emerges as an ultra-compact floating popup sheet over your current app or game instead of opening the full app.",
                    fontSize = 12.sp,
                    color = HermesTextSecondary,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        val popupIntent = Intent(context, HermesPopupActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        }
                        context.startActivity(popupIntent)
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = HermesCyan),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("test_popup_overlay_button")
                ) {
                    Text(
                        text = "🚀 Test Assistant Popup Overlay",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Screen Reading Vision on Trigger Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = HermesDarkSurface),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, HermesGold.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = HermesGold.copy(alpha = 0.15f),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = HermesGold,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Screen Reading Vision",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = HermesTextPrimary
                            )
                            Text(
                                text = "Trigger-Activated Screen Perception",
                                fontSize = 11.sp,
                                color = HermesGold
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = HermesSuccess.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "READY",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = HermesSuccess,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "When any trigger is activated, Hermes's Accessibility Service captures on-screen text and snapshots only when the trigger is pressed. Gemini Live 3.8 receives this visual context to answer questions like \"What's on my screen?\", summarize articles, or troubleshoot apps without leaving what you are doing.",
                    fontSize = 12.sp,
                    color = HermesTextSecondary,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = {
                        val popupIntent = Intent(context, HermesPopupActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        }
                        context.startActivity(popupIntent)
                    },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, HermesGold),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("test_screen_reading_button")
                ) {
                    Text(
                        text = "👁️ Test Screen Reading with Popup",
                        color = HermesGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // 1. Power Button / Assist Gesture Trigger
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = HermesDarkSurface),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, HermesDarkCardBorder, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = HermesCyan.copy(alpha = 0.15f),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(imageVector = Icons.Default.PowerSettingsNew, contentDescription = null, tint = HermesCyan, modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Power Button / Corner Swipe Trigger", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = HermesTextPrimary)
                        Text("Default Android Digital Assistant", fontSize = 11.sp, color = HermesCyan)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "By setting Hermes as your phone's default digital assistant, holding your phone's physical Power Button or swiping diagonally from either bottom corner will instantly activate Hermes voice listening from anywhere.",
                    fontSize = 12.sp,
                    color = HermesTextSecondary,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        try {
                            val intent = Intent(Settings.ACTION_VOICE_INPUT_SETTINGS).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            context.startActivity(intent)
                        } catch (_: Exception) {
                            val fallback = Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            context.startActivity(fallback)
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = HermesCyan),
                    modifier = Modifier.testTag("open_assistant_settings_button")
                ) {
                    Icon(imageVector = Icons.Default.Settings, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Set as Default Assistant in Settings", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        // 2. Volume Button Hotkey Trigger
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = HermesDarkSurface),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, HermesDarkCardBorder, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = HermesGold.copy(alpha = 0.15f),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(imageVector = Icons.Default.VolumeDown, contentDescription = null, tint = HermesGold, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Volume Key Hotkey", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = HermesTextPrimary)
                            Text("Double-tap Volume Down", fontSize = 11.sp, color = HermesGold)
                        }
                    }

                    Switch(
                        checked = volumeTriggerEnabled,
                        onCheckedChange = { checked ->
                            volumeTriggerEnabled = checked
                            viewModel.updateSettings(
                                botToken = settings?.telegramBotToken ?: "",
                                chatId = settings?.telegramChatId ?: "",
                                mode = settings?.mode ?: "HYBRID",
                                volumeTrigger = checked,
                                autoSpeak = autoSpeakEnabled,
                                pitch = speechPitch,
                                rate = speechRate
                            )
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = HermesGold,
                            uncheckedThumbColor = HermesTextSecondary,
                            uncheckedTrackColor = HermesDarkSurfaceVariant
                        ),
                        modifier = Modifier.testTag("volume_trigger_toggle_switch")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Allows quickly double-pressing the physical Volume Down key to trigger the assistant with haptic vibration. Requires enabling the Hermes Accessibility Service.",
                    fontSize = 12.sp,
                    color = HermesTextSecondary,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            context.startActivity(intent)
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("open_accessibility_settings_button")
                    ) {
                        Icon(imageVector = Icons.Default.Accessibility, contentDescription = null, tint = HermesGold, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Enable Service", fontSize = 11.sp, color = HermesGold)
                    }

                    Button(
                        onClick = {
                            TriggerEventBus.emitTrigger(TriggerSource.VOLUME_BUTTON_HOTKEY)
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = HermesGold),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("simulate_volume_hotkey_button")
                    ) {
                        Text("Test Hotkey", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }

        // 3. Quick Settings Tile
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = HermesDarkSurface),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, HermesDarkCardBorder, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = HermesCyan.copy(alpha = 0.15f),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(imageVector = Icons.Default.Tune, contentDescription = null, tint = HermesCyan, modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Quick Settings Drawer Tile", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = HermesTextPrimary)
                        Text("\"Hermes Listen\" 1-Tap Tile", fontSize = 11.sp, color = HermesCyan)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Swipe down twice from the top of your Android screen to open Quick Settings, tap the pencil/edit icon, and add 'Hermes Listen'. Tapping it launches voice listening instantly without needing to unlock the phone first.",
                    fontSize = 12.sp,
                    color = HermesTextSecondary,
                    lineHeight = 18.sp
                )
            }
        }

        // 4. Voice Speech Synthesis Preferences
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = HermesDarkSurface),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, HermesDarkCardBorder, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Auto-Speak Voice Responses", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = HermesTextPrimary)
                    Switch(
                        checked = autoSpeakEnabled,
                        onCheckedChange = { checked ->
                            autoSpeakEnabled = checked
                            viewModel.updateSettings(
                                botToken = settings?.telegramBotToken ?: "",
                                chatId = settings?.telegramChatId ?: "",
                                mode = settings?.mode ?: "HYBRID",
                                volumeTrigger = volumeTriggerEnabled,
                                autoSpeak = checked,
                                pitch = speechPitch,
                                rate = speechRate
                            )
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = HermesCyan,
                            uncheckedThumbColor = HermesTextSecondary,
                            uncheckedTrackColor = HermesDarkSurfaceVariant
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text("Speech Pitch: ${(speechPitch * 100).toInt()}%", fontSize = 12.sp, color = HermesTextSecondary)
                Slider(
                    value = speechPitch,
                    onValueChange = {
                        speechPitch = it
                        viewModel.updateSettings(
                            botToken = settings?.telegramBotToken ?: "",
                            chatId = settings?.telegramChatId ?: "",
                            mode = settings?.mode ?: "HYBRID",
                            volumeTrigger = volumeTriggerEnabled,
                            autoSpeak = autoSpeakEnabled,
                            pitch = it,
                            rate = speechRate
                        )
                    },
                    valueRange = 0.6f..1.5f,
                    colors = SliderDefaults.colors(thumbColor = HermesCyan, activeTrackColor = HermesCyan)
                )

                Text("Speech Speed: ${(speechRate * 100).toInt()}%", fontSize = 12.sp, color = HermesTextSecondary)
                Slider(
                    value = speechRate,
                    onValueChange = {
                        speechRate = it
                        viewModel.updateSettings(
                            botToken = settings?.telegramBotToken ?: "",
                            chatId = settings?.telegramChatId ?: "",
                            mode = settings?.mode ?: "HYBRID",
                            volumeTrigger = volumeTriggerEnabled,
                            autoSpeak = autoSpeakEnabled,
                            pitch = speechPitch,
                            rate = it
                        )
                    },
                    valueRange = 0.7f..1.6f,
                    colors = SliderDefaults.colors(thumbColor = HermesCyan, activeTrackColor = HermesCyan)
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedButton(
                    onClick = {
                        viewModel.voiceManager.speak(
                            "Hermes voice synthesis operational. Ready for your command.",
                            speechPitch,
                            speechRate
                        )
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = HermesCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Test Hermes Voice Playback", color = HermesCyan, fontSize = 12.sp)
                }
            }
        }

        // 5. Clear History Button
        OutlinedButton(
            onClick = { viewModel.clearHistory() },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = HermesError),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, HermesError.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
        ) {
            Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = HermesError, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Clear Chat & Execution History", color = HermesError, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}
