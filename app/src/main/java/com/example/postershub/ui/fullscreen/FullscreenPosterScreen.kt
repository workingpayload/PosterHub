package com.example.postershub.ui.fullscreen

import android.Manifest
import android.app.WallpaperManager
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.example.postershub.data.ImageUrl
import com.example.postershub.di.ServiceLocator
import com.example.postershub.domain.model.MediaType
import com.example.postershub.domain.model.PosterImage
import com.example.postershub.ui.components.MeteredConfirmDialog
import com.example.postershub.ui.components.ZoomableImage
import com.example.postershub.ui.nav.FullscreenRoute
import com.example.postershub.ui.nav.heroOrVariantKey
import com.example.postershub.util.ImageActions
import com.example.postershub.util.isMeteredConnection
import kotlinx.coroutines.launch

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun FullscreenPosterScreen(
    route: FullscreenRoute,
    sharedScope: SharedTransitionScope,
    animatedScope: AnimatedVisibilityScope,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val posters by produceState(initialValue = emptyList<PosterImage>(), route.movieId) {
        val type = if (route.isTv) MediaType.TV else MediaType.MOVIE
        value = runCatching { ServiceLocator.posterRepository.postersFor(route.movieId, type, route.posterPath) }
            .getOrDefault(emptyList())
    }
    val ready = posters.isNotEmpty()

    var pendingUrl by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }
    var isWallpapering by remember { mutableStateOf(false) }
    var pendingMeteredAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    val warnOnMetered by ServiceLocator.settingsStore.warnOnMetered.collectAsStateWithLifecycle(true)

    fun guardedRun(action: () -> Unit) {
        if (warnOnMetered && context.isMeteredConnection()) pendingMeteredAction = action else action()
    }

    val permLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        val url = pendingUrl
        pendingUrl = null
        if (granted && url != null) {
            scope.launch {
                isSaving = true
                val ok = ImageActions.savePoster(context, url, route.title)
                isSaving = false
                Toast.makeText(context, if (ok) "Saved to gallery" else "Save failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun save(url: String?) {
        url ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            scope.launch {
                isSaving = true
                val ok = ImageActions.savePoster(context, url, route.title)
                isSaving = false
                Toast.makeText(context, if (ok) "Saved to gallery" else "Save failed", Toast.LENGTH_SHORT).show()
            }
        } else {
            pendingUrl = url
            permLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

    fun wallpaper(url: String?) {
        url ?: return
        scope.launch {
            isWallpapering = true
            val ok = ImageActions.applyWallpaper(
                context, url, WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK
            )
            isWallpapering = false
            Toast.makeText(context, if (ok) "Wallpaper set" else "Couldn't set wallpaper", Toast.LENGTH_SHORT).show()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (!ready) {
            // Show the exact poster that was tapped while the variant list loads, so a variant
            // open doesn't flash the main poster first. Keyed so the enter morph starts from
            // (and, if backed out during load, returns to) the tapped element.
            val startKey = heroOrVariantKey(route.sharedKey, route.startIndex)
            with(sharedScope) {
                AsyncImage(
                    model = route.startUrl ?: ImageUrl.tmdbOriginal(route.posterPath),
                    contentDescription = route.title,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .sharedElement(
                            rememberSharedContentState(key = startKey),
                            animatedVisibilityScope = animatedScope,
                        ),
                )
            }
            CircularProgressIndicator(Modifier.align(Alignment.Center), color = Color.White)
        } else {
            // Now that the full list is known, open at the exact variant that was tapped.
            val pagerState = rememberPagerState(
                initialPage = route.startIndex.coerceIn(0, posters.lastIndex),
                pageCount = { posters.size },
            )
            // Key follows the current page, so pressing back closes onto THAT poster's origin:
            // the hero for the primary, or its own thumbnail in the variants strip.
            val currentKey = heroOrVariantKey(route.sharedKey, pagerState.currentPage)
            with(sharedScope) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxSize()
                        .sharedElement(
                            rememberSharedContentState(key = currentKey),
                            animatedVisibilityScope = animatedScope,
                        ),
                ) { page ->
                    ZoomableImage(
                        url = posters[page].url,
                        contentDescription = route.title,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }

            PageIndicator(
                current = pagerState.currentPage,
                total = posters.size,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp),
            )

            val currentUrl = posters.getOrNull(pagerState.currentPage)?.url
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OverlayIcon(Icons.Filled.Download, "Save", loading = isSaving) { guardedRun { save(currentUrl) } }
                OverlayIcon(Icons.Filled.Wallpaper, "Wallpaper", loading = isWallpapering) { guardedRun { wallpaper(currentUrl) } }
            }
        }

        OverlayIcon(
            icon = Icons.AutoMirrored.Filled.ArrowBack,
            label = "Back",
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(8.dp),
            onClick = onBack,
        )
    }

    pendingMeteredAction?.let { action ->
        MeteredConfirmDialog(
            onConfirm = { pendingMeteredAction = null; action() },
            onDismiss = { pendingMeteredAction = null },
        )
    }
}

/** Dots for a handful of variants; a "n / total" pill once there are too many to dot out. */
@Composable
private fun PageIndicator(current: Int, total: Int, modifier: Modifier = Modifier) {
    if (total <= 1) return
    if (total <= 12) {
        Row(modifier, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(total) { i ->
                val selected = i == current
                Box(
                    Modifier
                        .size(if (selected) 8.dp else 6.dp)
                        .clip(CircleShape)
                        .background(if (selected) Color.White else Color.White.copy(alpha = 0.4f))
                )
            }
        }
    } else {
        Box(
            modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Black.copy(alpha = 0.5f))
                .padding(horizontal = 10.dp, vertical = 4.dp),
        ) {
            Text("${current + 1} / $total", color = Color.White, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun OverlayIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    loading: Boolean = false,
    onClick: () -> Unit,
) {
    IconButton(
        onClick = onClick,
        enabled = !loading,
        modifier = modifier
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.4f)),
    ) {
        if (loading) {
            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
        } else {
            Icon(icon, contentDescription = label, tint = Color.White)
        }
    }
}
