package com.a21optimizer.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val SignalGreen = Color(0xFF50E3C2)
val SignalGreenDark = Color(0xFF006B59)
val TacticalBlue = Color(0xFF7DB7FF)
val WarningAmber = Color(0xFFFFB86B)
val DangerRed = Color(0xFFFF6B73)
val Ink = Color(0xFF080C12)
val Panel = Color(0xFF111923)
val PanelRaised = Color(0xFF17222F)
val TextPrimary = Color(0xFFE8F1F4)
val TextMuted = Color(0xFF9DADB5)

private val DarkColors = darkColorScheme(
    primary = SignalGreen,
    onPrimary = Color(0xFF002019),
    primaryContainer = Color(0xFF004D40),
    onPrimaryContainer = Color(0xFF78F8D8),
    secondary = TacticalBlue,
    onSecondary = Color(0xFF002F58),
    secondaryContainer = Color(0xFF163B5F),
    onSecondaryContainer = Color(0xFFD3E4FF),
    tertiary = WarningAmber,
    onTertiary = Color(0xFF492900),
    error = DangerRed,
    onError = Color(0xFF4A0006),
    background = Ink,
    onBackground = TextPrimary,
    surface = Panel,
    onSurface = TextPrimary,
    surfaceVariant = PanelRaised,
    onSurfaceVariant = TextMuted,
    outline = Color(0xFF394955),
    outlineVariant = Color(0xFF263540),
)

private val LightColors = lightColorScheme(
    primary = SignalGreenDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF9AF5DD),
    onPrimaryContainer = Color(0xFF002019),
    secondary = Color(0xFF315F88),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD0E5FF),
    onSecondaryContainer = Color(0xFF001D35),
    tertiary = Color(0xFF8B5000),
    onTertiary = Color.White,
    error = Color(0xFFBA1A1A),
    background = Color(0xFFF5F9FA),
    onBackground = Color(0xFF171D1F),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF171D1F),
    surfaceVariant = Color(0xFFE2EAED),
    onSurfaceVariant = Color(0xFF41494C),
    outline = Color(0xFF71797C),
    outlineVariant = Color(0xFFC1C8CB),
)

@Composable
fun A21OptimizerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        content = content,
    )
}
