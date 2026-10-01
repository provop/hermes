package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BrightnessHigh
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.HermesViewModel
import com.example.ui.components.ActionExecutionCard
import com.example.ui.theme.HermesCyan
import com.example.ui.theme.HermesDarkBackground
import com.example.ui.theme.HermesDarkCardBorder
import com.example.ui.theme.HermesDarkSurface
import com.example.ui.theme.HermesDarkSurfaceVariant
import com.example.ui.theme.HermesGold
import com.example.ui.theme.HermesTextPrimary
import com.example.ui.theme.HermesTextSecondary

@Composable
fun DeviceControlScreen(
    viewModel: HermesViewModel,
    modifier: Modifier = Modifier
) {
    val recentActions by viewModel.recentActions.collectAsState()
    var isFlashlightOn by remember { mutableStateOf(false) }

    // Dialog state for quick action modals
    var activeModalType by remember { mutableStateOf<String?>(null) }
    var modalInput1 by remember { mutableStateOf("") }
    var modalInput2 by remember { mutableStateOf("") }
    var modalInput3 by remember { mutableStateOf("") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(HermesDarkBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Phone & System Control",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = HermesTextPrimary
            )
            Text(
                text = "Direct hardware actions, navigation shortcuts, and permission controls.",
                fontSize = 12.sp,
                color = HermesTextSecondary,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        // Quick Utilities Row (Flashlight, Ringer Modes)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = HermesDarkSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, HermesDarkCardBorder, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Hardware Quick Toggles",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = HermesGold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = if (isFlashlightOn) HermesGold.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f),
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (isFlashlightOn) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
                                        contentDescription = null,
                                        tint = if (isFlashlightOn) HermesGold else HermesTextSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Flashlight Torch", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = HermesTextPrimary)
                                Text(if (isFlashlightOn) "Enabled" else "Disabled", fontSize = 11.sp, color = HermesTextSecondary)
                            }
                        }

                        Switch(
                            checked = isFlashlightOn,
                            onCheckedChange = { checked ->
                                isFlashlightOn = checked
                                viewModel.deviceController.toggleFlashlight(checked)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = HermesGold,
                                uncheckedThumbColor = HermesTextSecondary,
                                uncheckedTrackColor = HermesDarkSurfaceVariant
                            ),
                            modifier = Modifier.testTag("flashlight_toggle_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Ringer mode selector
                    Text("Ringer Mode:", fontSize = 11.sp, color = HermesTextSecondary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            Triple("Normal", "normal", Icons.Default.VolumeUp),
                            Triple("Vibrate", "vibrate", Icons.Default.VolumeDown),
                            Triple("Silent", "silent", Icons.Default.VolumeMute)
                        ).forEach { (label, mode, icon) ->
                            OutlinedButton(
                                onClick = { viewModel.deviceController.setRingerMode(mode) },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(14.dp), tint = HermesCyan)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(label, fontSize = 11.sp, color = HermesCyan)
                            }
                        }
                    }
                }
            }
        }

        // Phone & Communication Tools
        item {
            Text(
                text = "Assistant Daily Tasks",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = HermesGold
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ActionTile(
                    title = "Phone Call",
                    subtitle = "Dial or contact",
                    icon = Icons.Default.Call,
                    color = HermesCyan,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        activeModalType = "CALL"
                        modalInput1 = ""
                    }
                )
                ActionTile(
                    title = "Draft Email",
                    subtitle = "Compose mailto",
                    icon = Icons.Default.Email,
                    color = HermesCyan,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        activeModalType = "EMAIL"
                        modalInput1 = ""
                        modalInput2 = ""
                        modalInput3 = ""
                    }
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ActionTile(
                    title = "Send SMS",
                    subtitle = "Text message",
                    icon = Icons.Default.Message,
                    color = HermesCyan,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        activeModalType = "SMS"
                        modalInput1 = ""
                        modalInput2 = ""
                    }
                )
                ActionTile(
                    title = "Browse Web",
                    subtitle = "Search query",
                    icon = Icons.Default.Language,
                    color = HermesCyan,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        activeModalType = "BROWSER"
                        modalInput1 = ""
                    }
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ActionTile(
                    title = "Set Alarm",
                    subtitle = "Clock alarm",
                    icon = Icons.Default.Alarm,
                    color = HermesGold,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        activeModalType = "ALARM"
                        modalInput1 = "7"
                        modalInput2 = "0"
                        modalInput3 = "Hermes Morning Alarm"
                    }
                )
                ActionTile(
                    title = "Set Timer",
                    subtitle = "Countdown timer",
                    icon = Icons.Default.Timer,
                    color = HermesGold,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        activeModalType = "TIMER"
                        modalInput1 = "300"
                        modalInput2 = "Focus Session"
                    }
                )
            }
        }

        // Phone Settings & Permissions Direct Navigation
        item {
            Text(
                text = "Phone Settings & Permissions Navigation",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = HermesGold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val settingsItems = listOf(
                    Triple("App Permissions & Storage", "permissions", Icons.Default.Security),
                    Triple("Wi-Fi Settings", "wifi", Icons.Default.Wifi),
                    Triple("Bluetooth Settings", "bluetooth", Icons.Default.Bluetooth),
                    Triple("Display & Screen", "display", Icons.Default.BrightnessHigh),
                    Triple("Sound & Volumes", "sound", Icons.Default.VolumeUp),
                    Triple("Battery & Power Saver", "battery", Icons.Default.BatteryChargingFull),
                    Triple("Default Digital Assistant", "assistant", Icons.Default.Settings)
                )

                settingsItems.forEach { (name, key, icon) ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = HermesDarkSurface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, HermesDarkCardBorder, RoundedCornerShape(10.dp))
                            .clickable { viewModel.deviceController.openSettings(key) }
                            .testTag("settings_shortcut_$key")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = icon, contentDescription = null, tint = HermesCyan, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = name, fontSize = 13.sp, color = HermesTextPrimary, modifier = Modifier.weight(1f))
                            Text(text = "Open >", fontSize = 11.sp, color = HermesTextSecondary)
                        }
                    }
                }
            }
        }

        // Recent Executed Actions Log
        item {
            Text(
                text = "Recent Execution Log",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = HermesGold,
                modifier = Modifier.padding(top = 12.dp)
            )
        }

        if (recentActions.isEmpty()) {
            item {
                Text(
                    text = "No device actions executed yet. Give voice commands like \"Turn on flashlight\", \"Call John\", or \"Open Wi-Fi\".",
                    fontSize = 12.sp,
                    color = HermesTextSecondary
                )
            }
        } else {
            items(recentActions, key = { it.id }) { action ->
                ActionExecutionCard(action = action)
            }
        }
    }

    // Modal Dialogs for Quick Action Launches
    activeModalType?.let { modalType ->
        AlertDialog(
            onDismissRequest = { activeModalType = null },
            title = {
                Text(
                    text = when (modalType) {
                        "CALL" -> "Make Phone Call"
                        "EMAIL" -> "Draft Email"
                        "SMS" -> "Send SMS"
                        "BROWSER" -> "Browse Web / Search"
                        "ALARM" -> "Set Clock Alarm"
                        "TIMER" -> "Set Timer"
                        else -> "Execute Action"
                    },
                    color = HermesTextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    when (modalType) {
                        "CALL" -> {
                            OutlinedTextField(
                                value = modalInput1,
                                onValueChange = { modalInput1 = it },
                                label = { Text("Phone Number") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        "EMAIL" -> {
                            OutlinedTextField(
                                value = modalInput1,
                                onValueChange = { modalInput1 = it },
                                label = { Text("Recipient Email") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = modalInput2,
                                onValueChange = { modalInput2 = it },
                                label = { Text("Subject") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = modalInput3,
                                onValueChange = { modalInput3 = it },
                                label = { Text("Message Body") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        "SMS" -> {
                            OutlinedTextField(
                                value = modalInput1,
                                onValueChange = { modalInput1 = it },
                                label = { Text("Phone Number") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = modalInput2,
                                onValueChange = { modalInput2 = it },
                                label = { Text("Message Text") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        "BROWSER" -> {
                            OutlinedTextField(
                                value = modalInput1,
                                onValueChange = { modalInput1 = it },
                                label = { Text("Search Query or URL") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        "ALARM" -> {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = modalInput1,
                                    onValueChange = { modalInput1 = it },
                                    label = { Text("Hour (0-23)") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = modalInput2,
                                    onValueChange = { modalInput2 = it },
                                    label = { Text("Min (0-59)") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            OutlinedTextField(
                                value = modalInput3,
                                onValueChange = { modalInput3 = it },
                                label = { Text("Alarm Label") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        "TIMER" -> {
                            OutlinedTextField(
                                value = modalInput1,
                                onValueChange = { modalInput1 = it },
                                label = { Text("Duration (seconds)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = modalInput2,
                                onValueChange = { modalInput2 = it },
                                label = { Text("Timer Label") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        when (modalType) {
                            "CALL" -> viewModel.deviceController.makeCall(modalInput1)
                            "EMAIL" -> viewModel.deviceController.draftEmail(modalInput1, modalInput2, modalInput3)
                            "SMS" -> viewModel.deviceController.sendSms(modalInput1, modalInput2)
                            "BROWSER" -> viewModel.deviceController.browseWeb(modalInput1)
                            "ALARM" -> viewModel.deviceController.setAlarm(modalInput1.toIntOrNull() ?: 7, modalInput2.toIntOrNull() ?: 0, modalInput3)
                            "TIMER" -> viewModel.deviceController.setTimer(modalInput1.toIntOrNull() ?: 60, modalInput2)
                        }
                        activeModalType = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HermesCyan)
                ) {
                    Text("Execute", color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { activeModalType = null }) {
                    Text("Cancel", color = HermesTextSecondary)
                }
            },
            containerColor = HermesDarkSurface
        )
    }
}

@Composable
private fun ActionTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = HermesDarkSurface,
        modifier = modifier
            .border(1.dp, HermesDarkCardBorder, RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = color.copy(alpha = 0.15f),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = HermesTextPrimary)
                Text(text = subtitle, fontSize = 10.sp, color = HermesTextSecondary)
            }
        }
    }
}
