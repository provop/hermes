package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.AssistantStatus
import com.example.ui.theme.HermesCyan
import com.example.ui.theme.HermesCyanLight
import com.example.ui.theme.HermesError
import com.example.ui.theme.HermesGold
import com.example.ui.theme.HermesVoiceActiveGlow
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun VoiceOrbVisualizer(
    status: AssistantStatus,
    audioAmplitude: Float,
    modifier: Modifier = Modifier,
    size: Dp = 180.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "VoiceOrbTransition")

    // Pulse animation
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "OrbPulse"
    )

    // Rotation for thinking
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "OrbRotation"
    )

    // Wave ripple animation
    val rippleScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "OrbRipple"
    )

    val primaryColor = when (status) {
        AssistantStatus.IDLE -> HermesCyan
        AssistantStatus.LISTENING -> HermesVoiceActiveGlow
        AssistantStatus.THINKING -> HermesGold
        AssistantStatus.SPEAKING -> HermesCyanLight
        AssistantStatus.ERROR -> HermesError
    }

    val secondaryColor = when (status) {
        AssistantStatus.IDLE -> Color(0xFF003844)
        AssistantStatus.LISTENING -> HermesCyan
        AssistantStatus.THINKING -> Color(0xFFFF9E00)
        AssistantStatus.SPEAKING -> HermesGold
        AssistantStatus.ERROR -> Color(0xFF990022)
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val baseRadius = this.size.minDimension / 3.4f
            val dynamicRadius = baseRadius * pulseScale * (1f + (audioAmplitude * 0.45f))

            // Outer ripples when listening or speaking
            if (status == AssistantStatus.LISTENING || status == AssistantStatus.SPEAKING) {
                val rippleRadius = baseRadius * rippleScale * (1f + audioAmplitude * 0.5f)
                val rippleAlpha = (1f - (rippleScale - 1f) / 0.45f).coerceIn(0f, 0.6f)
                drawCircle(
                    color = primaryColor.copy(alpha = rippleAlpha),
                    radius = rippleRadius,
                    center = center,
                    style = Stroke(width = 3.dp.toPx())
                )

                // Secondary ripple
                val rippleRadius2 = baseRadius * (1f + (rippleScale - 1f) * 0.6f)
                drawCircle(
                    color = secondaryColor.copy(alpha = rippleAlpha * 0.7f),
                    radius = rippleRadius2,
                    center = center,
                    style = Stroke(width = 2.dp.toPx())
                )
            }

            // Orbiting nodes when thinking
            if (status == AssistantStatus.THINKING) {
                val orbitRadius = baseRadius * 1.3f
                for (i in 0 until 4) {
                    val angleRad = Math.toRadians((rotation + (i * 90)).toDouble())
                    val nodeX = center.x + (orbitRadius * cos(angleRad)).toFloat()
                    val nodeY = center.y + (orbitRadius * sin(angleRad)).toFloat()
                    drawCircle(
                        color = HermesGold,
                        radius = 4.dp.toPx(),
                        center = Offset(nodeX, nodeY)
                    )
                }
            }

            // Radial gradient glow
            val glowBrush = Brush.radialGradient(
                colors = listOf(
                    primaryColor.copy(alpha = 0.5f),
                    secondaryColor.copy(alpha = 0.2f),
                    Color.Transparent
                ),
                center = center,
                radius = dynamicRadius * 1.5f
            )
            drawCircle(
                brush = glowBrush,
                radius = dynamicRadius * 1.5f,
                center = center
            )

            // Inner solid glowing core
            val coreBrush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.9f),
                    primaryColor,
                    secondaryColor
                ),
                center = center,
                radius = dynamicRadius
            )
            drawCircle(
                brush = coreBrush,
                radius = dynamicRadius,
                center = center
            )

            // Cyber border ring
            drawCircle(
                color = primaryColor.copy(alpha = 0.8f),
                radius = dynamicRadius,
                center = center,
                style = Stroke(width = 2.5.dp.toPx())
            )
        }
    }
}
