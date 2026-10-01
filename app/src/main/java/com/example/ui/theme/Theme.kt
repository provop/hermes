package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val HermesDarkColorScheme = darkColorScheme(
    primary = HermesCyan,
    onPrimary = Color(0xFF002026),
    primaryContainer = Color(0xFF004D59),
    onPrimaryContainer = HermesCyanLight,
    secondary = HermesGold,
    onSecondary = Color(0xFF261900),
    secondaryContainer = Color(0xFF4D3600),
    onSecondaryContainer = Color(0xFFFFE799),
    tertiary = HermesVoiceActiveGlow,
    background = HermesDarkBackground,
    onBackground = HermesTextPrimary,
    surface = HermesDarkSurface,
    onSurface = HermesTextPrimary,
    surfaceVariant = HermesDarkSurfaceVariant,
    onSurfaceVariant = HermesTextSecondary,
    outline = HermesDarkCardBorder,
    error = HermesError,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = HermesDarkColorScheme,
        typography = Typography,
        content = content
    )
}

