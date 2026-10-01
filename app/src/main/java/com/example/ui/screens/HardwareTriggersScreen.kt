package com.example.ui.screens

import android.content.Intent
import android.provider.Settings
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
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.example.ui.theme.HermesTextPrimary
import com.example.ui.theme.HermesTextSecondary

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
            text = "Hardware Triggers & Shortcuts",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = HermesTextPrimary
        )
        Text(
            text = "Configure instant physical button access to awaken Hermes voice recognition.",
            fontSize = 12.sp,
            color = HermesTextSecondary,
            modifier = Modifier.padding(top = 2.dp)
        )

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
