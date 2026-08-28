package com.example.postershub.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import coil3.compose.AsyncImage
import kotlinx.coroutines.launch

/** Full-screen poster with pinch-zoom, pan, and spring-settled double-tap zoom. */
@Composable
fun ZoomableImage(
    url: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    val scale = remember { Animatable(1f) }
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    val target = (scale.value * zoom).coerceIn(1f, 5f)
                    scope.launch { scale.snapTo(target) }
                    if (target > 1f) {
                        scope.launch { offsetX.snapTo(offsetX.value + pan.x) }
                        scope.launch { offsetY.snapTo(offsetY.value + pan.y) }
                    } else {
                        scope.launch { offsetX.snapTo(0f) }
                        scope.launch { offsetY.snapTo(0f) }
                    }
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        scope.launch {
                            if (scale.value > 1f) {
                                launch { scale.animateTo(1f, spring()) }
                                launch { offsetX.animateTo(0f, spring()) }
                                launch { offsetY.animateTo(0f, spring()) }
                            } else {
                                scale.animateTo(
                                    2.5f,
                                    spring(dampingRatio = Spring.DampingRatioLowBouncy),
                                )
                            }
                        }
                    }
                )
            }
    ) {
        AsyncImage(
            model = url,
            contentDescription = contentDescription,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale.value
                    scaleY = scale.value
                    translationX = offsetX.value
                    translationY = offsetY.value
                },
        )
    }
}
