package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AlarmOn
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.service.ScreenContextHolder
import com.example.ui.AssistantStatus
import com.example.ui.HermesViewModel
import com.example.ui.theme.CalmCanvas
import com.example.ui.theme.CalmError
import com.example.ui.theme.CalmHairline
import com.example.ui.theme.CalmSignalAccent
import com.example.ui.theme.CalmSurface1
import com.example.ui.theme.CalmSurface2
import com.example.ui.theme.CalmTextMuted
import com.example.ui.theme.CalmTextPrimary
import com.example.ui.theme.CalmTextSecondary
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay

enum class CalmOverlayState {
    LISTENING,
    WORKING,
    DONE,
    REPLY,
    TYPING,
    ERROR,
    MINIMIZED
}

class HermesPopupActivity : ComponentActivity() {

    private val viewModel: HermesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MyApplicationTheme {
                var hasAudioPermission by remember {
                    mutableStateOf(
                        ContextCompat.checkSelfPermission(
                            this@HermesPopupActivity,
                            Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED
                    )
                }

                val permissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    hasAudioPermission = isGranted
                    if (isGranted) {
                        viewModel.startListening()
                    }
                }

                LaunchedEffect(Unit) {
                    if (hasAudioPermission) {
                        viewModel.startListening()
                    } else {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                }

                CalmInstrumentVoiceOverlay(
                    viewModel = viewModel,
                    hasAudioPermission = hasAudioPermission,
                    onRequestPermission = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                    onDismiss = { finish() },
                    onOpenFullApp = {
                        val fullAppIntent = Intent(this@HermesPopupActivity, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        }
                        startActivity(fullAppIntent)
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
fun CalmInstrumentVoiceOverlay(
    viewModel: HermesViewModel,
    hasAudioPermission: Boolean,
    onRequestPermission: () -> Unit,
    onDismiss: () -> Unit,
    onOpenFullApp: () -> Unit
) {
    val status by viewModel.status.collectAsState()
    val partialTranscript by viewModel.voiceManager.partialTranscript.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val settings by viewModel.settings.collectAsState()

    var overlayState by remember { mutableStateOf(CalmOverlayState.LISTENING) }
    var textInput by remember { mutableStateOf("") }
    val latestMessage = messages.lastOrNull()
    val hasScreenContext = remember { ScreenContextHolder.hasRecentScreenContext() }
    val modeLabel = if (settings?.mode == "HERMES_RELAY") "HERMES DIRECT" else "HYBRID"

    // Synchronize state based on ViewModel events
    LaunchedEffect(hasAudioPermission, status, latestMessage) {
        if (!hasAudioPermission) {
            overlayState = CalmOverlayState.ERROR
            return@LaunchedEffect
        }
        if (overlayState == CalmOverlayState.MINIMIZED || overlayState == CalmOverlayState.TYPING) return@LaunchedEffect

        when (status) {
            AssistantStatus.LISTENING -> overlayState = CalmOverlayState.LISTENING
            AssistantStatus.THINKING -> overlayState = CalmOverlayState.WORKING
            AssistantStatus.ERROR -> overlayState = CalmOverlayState.ERROR
            AssistantStatus.SPEAKING, AssistantStatus.IDLE -> {
                if (latestMessage != null && latestMessage.sender != "USER") {
                    if (latestMessage.content.contains("Execution completed", ignoreCase = true) ||
                        latestMessage.content.contains("alarm", ignoreCase = true) ||
                        latestMessage.content.contains("timer", ignoreCase = true) ||
                        latestMessage.content.contains("Action", ignoreCase = true) ||
                        latestMessage.sender == "SYSTEM"
                    ) {
                        overlayState = CalmOverlayState.DONE
                    } else {
                        overlayState = CalmOverlayState.REPLY
                    }
                }
            }
        }
    }

    // Full-screen 55% dark scrim over whatever app is behind it (Design Spec Note 2)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x8C111110))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                if (overlayState != CalmOverlayState.MINIMIZED) onDismiss()
            },
        contentAlignment = if (overlayState == CalmOverlayState.MINIMIZED) Alignment.CenterEnd else Alignment.BottomCenter
    ) {
        AnimatedContent(
            targetState = overlayState,
            label = "calm_overlay_states"
        ) { state ->
            when (state) {
                CalmOverlayState.MINIMIZED -> {
                    // Overlay 7: Minimized Chip (PDF p. 17 & p. 37)
                    CalmMinimizedChip(
                        onExpand = { overlayState = CalmOverlayState.LISTENING }
                    )
                }

                CalmOverlayState.TYPING -> {
                    // Overlay 5: Typing Mode (PDF p. 15 & p. 34)
                    CalmTypingOverlay(
                        text = textInput,
                        onTextChange = { textInput = it },
                        onSend = {
                            if (textInput.isNotBlank()) {
                                viewModel.handleUserInput(textInput)
                                textInput = ""
                                overlayState = CalmOverlayState.WORKING
                            }
                        },
                        onSwitchToMic = {
                            viewModel.startListening()
                            overlayState = CalmOverlayState.LISTENING
                        },
                        onDismiss = onDismiss
                    )
                }

                else -> {
                    // Bottom-centered voice overlay at ~72px above bottom edge (Design Spec Note 3)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .padding(bottom = 72.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { /* Consume taps */ },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Upper Context Area (Live Transcript / Working Banner / Result Card)
                        when (state) {
                            CalmOverlayState.LISTENING -> {
                                // Overlay 1: Live Transcript above orb (PDF p. 11 & p. 30)
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(bottom = 44.dp)
                                ) {
                                    if (hasScreenContext) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .clip(CircleShape)
                                                    .background(CalmSignalAccent)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "SCREEN_VISION ACTIVE",
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = CalmSignalAccent,
                                                letterSpacing = 1.sp
                                            )
                                        }
                                    }

                                    val displayText = if (partialTranscript.isNotBlank()) {
                                        partialTranscript
                                    } else {
                                        "Listening to your command..."
                                    }

                                    Text(
                                        text = displayText,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (partialTranscript.isNotBlank()) CalmTextPrimary else CalmTextSecondary,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 26.sp,
                                        modifier = Modifier.padding(horizontal = 20.dp)
                                    )
                                }
                            }

                            CalmOverlayState.WORKING -> {
                                // Overlay 2: Working State (PDF p. 12 & p. 31)
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(bottom = 36.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(bottom = 6.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(CalmSignalAccent)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "GEMINI_REASONING · ACTIVE",
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = CalmSignalAccent,
                                            letterSpacing = 1.sp
                                        )
                                    }

                                    Text(
                                        text = if (partialTranscript.isNotBlank()) "“$partialTranscript”" else "“Processing query...”",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = CalmTextPrimary,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            CalmOverlayState.DONE -> {
                                // Overlay 3: Done Action Card (PDF p. 13 & p. 32)
                                CalmDoneResultCard(
                                    content = latestMessage?.content ?: "Alarm set for 07:00 tomorrow",
                                    onUndo = { onDismiss() },
                                    onOpenHermes = onOpenFullApp,
                                    modifier = Modifier.padding(bottom = 24.dp)
                                )
                            }

                            CalmOverlayState.REPLY -> {
                                // Overlay 4: Agent Reply Card (PDF p. 14 & p. 33)
                                CalmReplyCard(
                                    replyText = latestMessage?.content ?: "",
                                    onOpenHermes = onOpenFullApp,
                                    modifier = Modifier.padding(bottom = 24.dp)
                                )
                            }

                            CalmOverlayState.ERROR -> {
                                // Overlay 6: Error Headline (PDF p. 16 & p. 36)
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(bottom = 36.dp)
                                ) {
                                    Text(
                                        text = if (!hasAudioPermission) "Mic permission off" else "Command error",
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = CalmError
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = CalmSurface1,
                                        border = BorderStroke(1.dp, CalmHairline),
                                        modifier = Modifier.clickable {
                                            if (!hasAudioPermission) onRequestPermission() else viewModel.startListening()
                                        }
                                    ) {
                                        Text(
                                            text = "Fix",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = CalmTextPrimary,
                                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }

                            else -> Unit
                        }

                        // Center Instrument Core: Keyboard (44px) + Voice Orb (96px) + Close (44px)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Left Accessory Button: Keyboard Input (44px true circle, PDF p. 1 & p. 30)
                            Surface(
                                shape = CircleShape,
                                color = CalmSurface1,
                                border = BorderStroke(1.dp, Color(0x1AFFFFFF)),
                                modifier = Modifier
                                    .size(44.dp)
                                    .clickable { overlayState = CalmOverlayState.TYPING }
                                    .testTag("overlay_keyboard_button")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Keyboard,
                                        contentDescription = "Switch to keyboard input",
                                        tint = CalmTextPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(24.dp))

                            // Center Voice Orb (96px true circle with 108px/102px accent ring, PDF p. 1 & p. 30-33)
                            CalmVoiceOrbCore(
                                state = state,
                                onClick = {
                                    if (state == CalmOverlayState.LISTENING) {
                                        viewModel.stopListening()
                                    } else {
                                        viewModel.startListening()
                                        overlayState = CalmOverlayState.LISTENING
                                    }
                                }
                            )

                            Spacer(modifier = Modifier.width(24.dp))

                            // Right Accessory Button: Close / Dismiss (44px true circle, PDF p. 1 & p. 30)
                            Surface(
                                shape = CircleShape,
                                color = CalmSurface1,
                                border = BorderStroke(1.dp, Color(0x1AFFFFFF)),
                                modifier = Modifier
                                    .size(44.dp)
                                    .clickable { onDismiss() }
                                    .testTag("overlay_close_button")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Dismiss voice overlay",
                                        tint = CalmTextPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Utilitarian Monospaced Mode Descriptor (HYBRID / HERMES DIRECT)
                        Text(
                            text = modeLabel,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = CalmTextSecondary,
                            letterSpacing = 2.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CalmVoiceOrbCore(
    state: CalmOverlayState,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orb_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val spinRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin_rot"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(116.dp)
            .clickable { onClick() }
            .testTag("voice_orb_center")
    ) {
        // Outer Accent Ring based on state
        when (state) {
            CalmOverlayState.LISTENING -> {
                // 108px Orange Pulse Ring
                Box(
                    modifier = Modifier
                        .size(108.dp)
                        .scale(pulseScale)
                        .border(2.dp, CalmSignalAccent, CircleShape)
                )
            }

            CalmOverlayState.WORKING -> {
                // Spinning arc ring
                Box(
                    modifier = Modifier
                        .size(108.dp)
                        .rotate(spinRotation)
                        .border(2.dp, CalmSignalAccent, CircleShape)
                )
            }

            CalmOverlayState.DONE, CalmOverlayState.REPLY -> {
                // Static 102px accent ring
                Box(
                    modifier = Modifier
                        .size(104.dp)
                        .border(2.dp, CalmSignalAccent, CircleShape)
                )
            }

            CalmOverlayState.ERROR -> {
                // Error Red ring
                Box(
                    modifier = Modifier
                        .size(104.dp)
                        .border(2.dp, CalmError, CircleShape)
                )
            }

            else -> Unit
        }

        // Main Center Surface: 96px true circle in #1A1A18
        Surface(
            shape = CircleShape,
            color = CalmSurface1,
            border = BorderStroke(1.dp, Color(0x1AFFFFFF)),
            modifier = Modifier.size(96.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                when (state) {
                    CalmOverlayState.DONE -> {
                        // Geometric Success Vector (Done State, PDF p. 13 & p. 32)
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Success",
                            tint = CalmSignalAccent,
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    CalmOverlayState.ERROR -> {
                        // Utilitarian Null Dash (Error State, PDF p. 16 & p. 36)
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Error",
                            tint = CalmError,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    else -> {
                        // 5 Vertical Equalizer Bars in #E8743B (PDF p. 11, 14, 30, 33)
                        CalmEqualizerBars(isAnimated = state == CalmOverlayState.LISTENING || state == CalmOverlayState.REPLY)
                    }
                }
            }
        }
    }
}

@Composable
fun CalmEqualizerBars(isAnimated: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "eq_bars")
    val barHeight1 by infiniteTransition.animateFloat(
        initialValue = 12f,
        targetValue = 24f,
        animationSpec = infiniteRepeatable(tween(260, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "b1"
    )
    val barHeight2 by infiniteTransition.animateFloat(
        initialValue = 24f,
        targetValue = 42f,
        animationSpec = infiniteRepeatable(tween(320, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "b2"
    )
    val barHeight3 by infiniteTransition.animateFloat(
        initialValue = 38f,
        targetValue = 54f,
        animationSpec = infiniteRepeatable(tween(290, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "b3"
    )
    val barHeight4 by infiniteTransition.animateFloat(
        initialValue = 20f,
        targetValue = 36f,
        animationSpec = infiniteRepeatable(tween(340, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "b4"
    )
    val barHeight5 by infiniteTransition.animateFloat(
        initialValue = 14f,
        targetValue = 26f,
        animationSpec = infiniteRepeatable(tween(270, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "b5"
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val heights = if (isAnimated) {
            listOf(barHeight1, barHeight2, barHeight3, barHeight4, barHeight5)
        } else {
            listOf(14f, 26f, 38f, 22f, 14f)
        }

        heights.forEach { h ->
            Box(
                modifier = Modifier
                    .width(3.5.dp)
                    .height(h.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(CalmSignalAccent)
            )
        }
    }
}

@Composable
fun CalmDoneResultCard(
    content: String,
    onUndo: () -> Unit,
    onOpenHermes: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Card Container: 340px max width, #1A1A18, 12px radius (PDF p. 13 & p. 32)
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = CalmSurface1,
        border = BorderStroke(1.dp, CalmHairline),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Action Completed",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = CalmTextPrimary
                    )
                    Text(
                        text = content,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        color = CalmTextSecondary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = CalmSurface2,
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.AlarmOn,
                            contentDescription = null,
                            tint = CalmSignalAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Micro telemetry readout row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "EXEC_ID // 0x3F82",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = CalmTextMuted
                )
                Text(
                    text = "ACK_PROMPT_DONE",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = CalmSignalAccent
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(CalmHairline))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Dismiss",
                    fontSize = 13.sp,
                    color = CalmTextSecondary,
                    modifier = Modifier.clickable { onUndo() }
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onOpenHermes() }
                ) {
                    Text(
                        text = "Open Hermes",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = CalmSignalAccent
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = CalmSignalAccent,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CalmReplyCard(
    replyText: String,
    onOpenHermes: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    // Dynamic Live Result Card with Left Accent Rule (PDF p. 14 & p. 33)
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = CalmSurface1,
        border = BorderStroke(1.dp, CalmHairline),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(CalmSignalAccent)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "HERMES SYNTHESIS",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = CalmSignalAccent,
                        letterSpacing = 1.sp
                    )
                }

                Text(
                    text = "0.14s",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = CalmTextMuted
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Body with left accent rail
            Row(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(48.dp)
                        .clip(RoundedCornerShape(1.dp))
                        .background(CalmSignalAccent)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = replyText,
                    fontSize = 14.sp,
                    color = CalmTextPrimary,
                    lineHeight = 20.sp,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "@hermes_bot",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = CalmTextMuted
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { expanded = !expanded }
                ) {
                    Text(
                        text = if (expanded) "Collapse" else "Expand",
                        fontSize = 12.sp,
                        color = CalmSignalAccent
                    )
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = CalmSignalAccent,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CalmSurface2, RoundedCornerShape(6.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = "DISPATCH: LOCAL_GEMINI_3.5",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = CalmTextSecondary
                    )
                    Text(
                        text = "LATENCY_DELTA: -14.2ms",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = CalmSignalAccent
                    )
                }
            }
        }
    }
}

@Composable
fun CalmTypingOverlay(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    onSwitchToMic: () -> Unit,
    onDismiss: () -> Unit
) {
    // Overlay 5: Typing State (PDF p. 15 & p. 34-35)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Status Pill
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = CalmSurface1,
            border = BorderStroke(1.dp, CalmHairline),
            modifier = Modifier.padding(bottom = 6.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(CalmSignalAccent)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Voice input paused · type command",
                    fontSize = 12.sp,
                    color = CalmTextSecondary
                )
            }
        }

        Text(
            text = "KERNEL DSP IDLE • TTY_INPUT_READY",
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            color = CalmTextMuted,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        // Command Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Mini Waveform Icon
            Surface(
                shape = CircleShape,
                color = CalmSurface1,
                modifier = Modifier
                    .size(44.dp)
                    .clickable { onSwitchToMic() }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Switch to mic",
                        tint = CalmSignalAccent,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Text Input Pill
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = CalmSurface1,
                border = BorderStroke(1.dp, CalmHairline),
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BasicTextField(
                        value = text,
                        onValueChange = onTextChange,
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = 13.sp,
                            color = CalmTextPrimary
                        ),
                        cursorBrush = SolidColor(CalmSignalAccent),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = { onSend() }),
                        decorationBox = { innerTextField ->
                            if (text.isEmpty()) {
                                Text(
                                    text = "Type command or query…",
                                    fontSize = 13.sp,
                                    color = CalmTextMuted
                                )
                            }
                            innerTextField()
                        },
                        modifier = Modifier.weight(1f).testTag("calm_typing_input")
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Orange Send Button
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = CalmSignalAccent,
                modifier = Modifier
                    .size(44.dp)
                    .clickable { onSend() }
                    .testTag("calm_typing_send")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.ArrowUpward,
                        contentDescription = "Send",
                        tint = CalmCanvas,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CalmMinimizedChip(
    onExpand: () -> Unit
) {
    // Overlay 7: Minimized Chip (PDF p. 17 & p. 37)
    var elapsedSeconds by remember { mutableIntStateOf(14) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            elapsedSeconds++
        }
    }

    Row(
        modifier = Modifier
            .padding(end = 12.dp)
            .clickable { onExpand() }
            .testTag("calm_minimized_chip"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Expand Hint Pill
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = CalmSurface1,
            border = BorderStroke(1.dp, CalmHairline),
            modifier = Modifier.padding(end = 8.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(CalmSignalAccent)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "TAP TO EXPAND",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = CalmTextSecondary,
                    letterSpacing = 1.sp
                )
            }
        }

        // Circular Orb Node (48px true circle)
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                shape = CircleShape,
                color = CalmSurface1,
                border = BorderStroke(1.dp, CalmSignalAccent),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(CalmSignalAccent))
                        Box(modifier = Modifier.width(2.dp).height(10.dp).clip(RoundedCornerShape(1.dp)).background(CalmSignalAccent))
                        Box(modifier = Modifier.width(2.dp).height(14.dp).clip(RoundedCornerShape(1.dp)).background(CalmSignalAccent))
                        Box(modifier = Modifier.width(2.dp).height(8.dp).clip(RoundedCornerShape(1.dp)).background(CalmSignalAccent))
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Timer Pill
            Text(
                text = String.format("%02d:%02d", elapsedSeconds / 60, elapsedSeconds % 60),
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = CalmTextSecondary
            )
        }
    }
}
