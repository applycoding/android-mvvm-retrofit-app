package com.arpanbom.pulse.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class PulseTones(
    val up: Color,
    val down: Color,
    val live: Color,
    val line: Color,
)

val LocalPulseTones = staticCompositionLocalOf {
    PulseTones(up = UpDark, down = DownDark, live = Gold, line = LineDark)
}

private val DarkColors = darkColorScheme(
    primary = Gold,
    onPrimary = Color(0xFF2A210E),
    background = Ink,
    onBackground = Color(0xFFF3F6F1),
    surface = SurfaceDark,
    onSurface = Color(0xFFF3F6F1),
    surfaceVariant = Color(0xFF243026),
    onSurfaceVariant = MutedDark,
    outline = LineDark,
)

private val LightColors = lightColorScheme(
    primary = GoldDeep,
    onPrimary = Color.White,
    background = Paper,
    onBackground = Color(0xFF172117),
    surface = SurfaceLight,
    onSurface = Color(0xFF172117),
    surfaceVariant = Color(0xFFE7EFE4),
    onSurfaceVariant = MutedLight,
    outline = LineLight,
)

@Composable
fun PulseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val tones = if (darkTheme) {
        PulseTones(up = UpDark, down = DownDark, live = Gold, line = LineDark)
    } else {
        PulseTones(up = UpLight, down = DownLight, live = GoldDeep, line = LineLight)
    }
    CompositionLocalProvider(LocalPulseTones provides tones) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColors else LightColors,
            typography = PulseTypography,
            content = content,
        )
    }
}
