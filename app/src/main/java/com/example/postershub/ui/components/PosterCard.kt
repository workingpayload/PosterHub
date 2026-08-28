package com.example.postershub.ui.components

import android.content.Intent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.postershub.data.ImageUrl
import com.example.postershub.data.local.FavoriteMovie
import com.example.postershub.di.ServiceLocator
import com.example.postershub.domain.model.Movie
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * A tappable poster with shimmer placeholder, spring press-scale, and a shared-element key.
 * Long-press opens a quick Favorite/Share menu without navigating to the detail screen.
 */
@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalFoundationApi::class)
@Composable
fun SharedTransitionScope.PosterCard(
    movie: Movie,
    sharedKey: String,
    animatedScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier,
    showTypeBadge: Boolean = false,
    onClick: () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.94f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "press-scale",
    )

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    var showMenu by remember { mutableStateOf(false) }
    var isFavorite by remember { mutableStateOf(false) }

    LaunchedEffect(showMenu) {
        if (showMenu) {
            isFavorite = ServiceLocator.favoritesStore.favorites.first().any { it.id == movie.id }
        }
    }

    Box(
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .aspectRatio(2f / 3f)
            .clip(RoundedCornerShape(14.dp))
            .sharedElement(
                rememberSharedContentState(key = sharedKey),
                animatedVisibilityScope = animatedScope,
            )
            .combinedClickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick,
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    showMenu = true
                },
            )
    ) {
        ShimmerBox(Modifier.fillMaxSize())
        AsyncImage(
            model = ImageUrl.tmdbThumb(movie.posterPath),
            contentDescription = movie.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        if (showTypeBadge) {
            Text(
                if (movie.isTv) "TV" else "Movie",
                color = Color.White,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(6.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.Black.copy(alpha = 0.55f))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
            )
        }

        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
            DropdownMenuItem(
                text = { Text(if (isFavorite) "Remove Favorite" else "Add Favorite") },
                leadingIcon = {
                    Icon(if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder, contentDescription = null)
                },
                onClick = {
                    showMenu = false
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    val posterUrl = ImageUrl.tmdbOriginal(movie.posterPath) ?: return@DropdownMenuItem
                    scope.launch {
                        ServiceLocator.favoritesStore.toggle(
                            FavoriteMovie(
                                id = movie.id,
                                title = movie.title,
                                posterPath = movie.posterPath,
                                posterUrl = posterUrl,
                                addedAt = System.currentTimeMillis(),
                                isTv = movie.isTv,
                                voteAverage = movie.voteAverage,
                            )
                        )
                    }
                },
            )
            DropdownMenuItem(
                text = { Text("Share") },
                leadingIcon = { Icon(Icons.Filled.Share, contentDescription = null) },
                onClick = {
                    showMenu = false
                    val url = "https://www.themoviedb.org/${if (movie.isTv) "tv" else "movie"}/${movie.id}"
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, "Check out ${movie.title} on TMDB: $url")
                    }
                    context.startActivity(Intent.createChooser(intent, "Share"))
                },
            )
        }
    }
}
