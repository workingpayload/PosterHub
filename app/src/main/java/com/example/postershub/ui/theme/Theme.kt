package com.example.postershub.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

// PostersHub defaults to a bespoke cinematic dark scheme. Users can opt into following the
// system theme (light/dark + Material You dynamic color) from Settings.
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
    useSystemTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val dark = isSystemInDarkTheme()

    val colorScheme = when {
        !useSystemTheme -> CinematicColors
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        dark -> darkColorScheme()
        else -> lightColorScheme()
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
