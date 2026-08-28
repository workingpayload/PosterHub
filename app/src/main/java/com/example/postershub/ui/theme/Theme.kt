package com.example.postershub.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

// PostersHub is a cinematic, dark-first experience: we intentionally pin a bespoke dark
// scheme rather than following the system light/dark or Material You dynamic color.
private val CinematicColors = darkColorScheme(
    primary = Electric,
    onPrimary = OnInk,
    secondary = Gold,
    onSecondary = Ink,
    tertiary = ElectricSoft,
    background = Ink,
    onBackground = OnInk,
    surface = InkElevated,
    onSurface = OnInk,
    surfaceVariant = InkCard,
    onSurfaceVariant = Mist,
)

@Composable
fun PostershubTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CinematicColors,
        typography = Typography,
        content = content
    )
}
