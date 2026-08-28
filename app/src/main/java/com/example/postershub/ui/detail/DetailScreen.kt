package com.example.postershub.ui.detail

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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.example.postershub.data.ImageUrl
import com.example.postershub.domain.model.PosterImage
import com.example.postershub.ui.components.ShimmerBox
import com.example.postershub.ui.components.DynamicBackground
import com.example.postershub.ui.nav.DetailRoute
import com.example.postershub.ui.nav.variantThumbKey
import com.example.postershub.ui.theme.Gold
import com.example.postershub.ui.theme.Mist
import com.example.postershub.util.ImageActions
import com.example.postershub.util.viewModelFactory
import kotlinx.coroutines.launch

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun DetailScreen(
    route: DetailRoute,
    sharedScope: SharedTransitionScope,
    animatedScope: AnimatedVisibilityScope,
    onBack: () -> Unit,
    onOpenFullscreen: (startIndex: Int, startUrl: String?) -> Unit,
    viewModel: DetailViewModel = viewModel(factory = viewModelFactory { DetailViewModel(route.movieId, route.isTv, route.posterPath) }),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isFavorite by viewModel.isFavorite.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScopeSafe()
    val scroll = rememberScrollState()

    val primaryUrl = state.posters.firstOrNull()?.url ?: ImageUrl.tmdbOriginal(route.posterPath)
    // posters[0] is the tapped/original poster (pinned first in PosterRepository), which is also
    // what the hero tap opens at fullscreen index 0. Fall back to the passed path during load.
    val heroUrl = state.posters.firstOrNull()?.url ?: ImageUrl.tmdb(route.posterPath, "w780")

    var showWallpaperDialog by remember { mutableStateOf(false) }
    var pendingUrl by remember { mutableStateOf<String?>(null) }

    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        val url = pendingUrl
        pendingUrl = null
        if (granted && url != null) {
            scope.launch {
                val ok = ImageActions.savePoster(context, url, route.title)
                Toast.makeText(context, if (ok) "Saved to gallery" else "Save failed", Toast.LENGTH_SHORT).show()
            }
        } else if (!granted) {
            Toast.makeText(context, "Storage permission needed to save", Toast.LENGTH_SHORT).show()
        }
    }

    fun download() {
        val url = primaryUrl ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            scope.launch {
                val ok = ImageActions.savePoster(context, url, route.title)
                Toast.makeText(context, if (ok) "Saved to gallery" else "Save failed", Toast.LENGTH_SHORT).show()
            }
        } else {
            pendingUrl = url
            permLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

    fun applyWallpaper(which: Int) {
        val url = primaryUrl ?: return
        showWallpaperDialog = false
        scope.launch {
            val ok = ImageActions.applyWallpaper(context, url, which)
            Toast.makeText(context, if (ok) "Wallpaper set" else "Couldn't set wallpaper", Toast.LENGTH_SHORT).show()
        }
    }

    DynamicBackground(posterUrl = primaryUrl, modifier = Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(scroll)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(24.dp))

            // Hero poster (shared element) with subtle parallax drift.
            with(sharedScope) {
                Box(
                    modifier = Modifier
                        .graphicsLayer { translationY = scroll.value * 0.3f }
                        .padding(horizontal = 40.dp)
                        .fillMaxWidth()
                        .aspectRatio(2f / 3f)
                        .clip(RoundedCornerShape(20.dp))
                        .sharedElement(
                            rememberSharedContentState(key = route.sharedKey),
                            animatedVisibilityScope = animatedScope,
                        )
                        .clickable { onOpenFullscreen(0, primaryUrl) }
                ) {
                    ShimmerBox(Modifier.fillMaxSize(), RoundedCornerShape(20.dp))
                    AsyncImage(
                        model = heroUrl,
                        contentDescription = route.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = state.movie?.title ?: route.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 24.dp),
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 6.dp),
            ) {
                if (route.isTv) Text("TV Series", color = Gold, fontWeight = FontWeight.SemiBold)
                state.movie?.year?.let { Text(it, color = Mist) }
                state.movie?.let {
                    Icon(Icons.Filled.Star, contentDescription = null, tint = Gold, modifier = Modifier.size(16.dp))
                    Text(String.format("%.1f", it.voteAverage), color = Mist)
                }
                if (state.posters.isNotEmpty()) {
                    val best = state.posters.first()
                    Text("• ${best.width}×${best.height}", color = Mist)
                }
            }

            // Actions
            Row(
                horizontalArrangement = Arrangement.spacedBy(28.dp),
                modifier = Modifier.padding(vertical = 18.dp),
            ) {
                ActionButton(
                    icon = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    label = "Favorite",
                    tint = if (isFavorite) Gold else Color.White,
                    onClick = { viewModel.toggleFavorite(route.title, route.posterPath) },
                )
                ActionButton(Icons.Filled.Download, "Save") { download() }
                ActionButton(Icons.Filled.Wallpaper, "Wallpaper") { showWallpaperDialog = true }
            }

            state.movie?.overview?.takeIf { it.isNotBlank() }?.let { overview ->
                Text(
                    overview,
                    color = Mist,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 24.dp),
                )
            }

            if (state.posters.size > 1) {
                Text(
                    "Poster variants (${state.posters.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, top = 20.dp, bottom = 8.dp),
                )
                VariantsStrip(
                    posters = state.posters,
                    sharedScope = sharedScope,
                    animatedScope = animatedScope,
                    baseKey = route.sharedKey,
                    onClick = onOpenFullscreen,
                )
            }

            if (state.loading) {
                Spacer(Modifier.height(20.dp))
                CircularProgressIndicator()
            }
        }

        // Back button overlay
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .statusBarsPadding()
                .padding(8.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.35f)),
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
        }
    }

    if (showWallpaperDialog) {
        AlertDialog(
            onDismissRequest = { showWallpaperDialog = false },
            title = { Text("Set wallpaper") },
            text = { Text("Where should this poster be applied?") },
            confirmButton = {
                TextButton(onClick = { applyWallpaper(WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK) }) {
                    Text("Both")
                }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = { applyWallpaper(WallpaperManager.FLAG_SYSTEM) }) { Text("Home") }
                    TextButton(onClick = { applyWallpaper(WallpaperManager.FLAG_LOCK) }) { Text("Lock") }
                }
            },
        )
    }
}

@Composable
private fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color = Color.White,
    onClick: () -> Unit,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(onClick = onClick) {
            Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(28.dp))
        }
        Text(label, style = MaterialTheme.typography.labelSmall, color = Mist)
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun VariantsStrip(
    posters: List<PosterImage>,
    sharedScope: SharedTransitionScope,
    animatedScope: AnimatedVisibilityScope,
    baseKey: String,
    onClick: (Int, String?) -> Unit,
) {
    LazyRow(
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(posters.size) { index ->
            val poster = posters[index]
            val thumbKey = variantThumbKey(baseKey, index)
            with(sharedScope) {
                Box(
                    Modifier
                        .width(110.dp)
                        .aspectRatio(2f / 3f)
                        .clip(RoundedCornerShape(12.dp))
                        .sharedElement(
                            rememberSharedContentState(key = thumbKey),
                            animatedVisibilityScope = animatedScope,
                        )
                        .clickable { onClick(index, poster.url) }
                ) {
                    ShimmerBox(Modifier.fillMaxSize(), RoundedCornerShape(12.dp))
                    AsyncImage(
                        model = poster.thumbUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}

@Composable
private fun rememberCoroutineScopeSafe() = androidx.compose.runtime.rememberCoroutineScope()
