package com.example.postershub.ui.components

import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.palette.graphics.Palette
import coil3.compose.AsyncImage
import com.example.postershub.ui.theme.Electric
import com.example.postershub.ui.theme.Ink
import com.example.postershub.util.ImageActions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Extracts dominant colours from [posterUrl] via Palette and paints an animated gradient
 * plus a blurred poster wash behind [content]. Falls back to the app accent while loading.
 */
@Composable
fun DynamicBackground(
    posterUrl: String?,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val context = LocalContext.current
    var topColor by remember(posterUrl) { mutableStateOf(Electric) }
    var bottomColor by remember(posterUrl) { mutableStateOf(Ink) }

    LaunchedEffect(posterUrl) {
        if (posterUrl == null) return@LaunchedEffect
        val bmp = ImageActions.loadBitmap(context, posterUrl) ?: return@LaunchedEffect
        val palette = withContext(Dispatchers.Default) { Palette.from(bmp).generate() }
        val vibrant = palette.getVibrantColor(Electric.toArgb())
        val dark = palette.getDarkMutedColor(Ink.toArgb())
        topColor = Color(vibrant)
        bottomColor = Color(dark)
    }

    val animatedTop by animateColorAsState(topColor, tween(700), label = "bg-top")
    val animatedBottom by animateColorAsState(bottomColor, tween(700), label = "bg-bottom")

    Box(modifier.background(Ink)) {
        if (posterUrl != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AsyncImage(
                model = posterUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(80.dp)
                    .alpha(0.45f),
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to animatedTop.copy(alpha = 0.80f),
                        0.6f to animatedBottom.copy(alpha = 0.92f),
                        1f to Ink,
                    )
                )
        )
        content()
    }
}
