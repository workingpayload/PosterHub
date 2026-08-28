package com.example.postershub.ui.favorites

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.example.postershub.data.ImageUrl
import com.example.postershub.data.local.FavoriteMovie
import com.example.postershub.ui.components.ShimmerBox
import com.example.postershub.ui.theme.Mist
import com.example.postershub.util.viewModelFactory

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun FavoritesScreen(
    sharedScope: SharedTransitionScope,
    animatedScope: AnimatedVisibilityScope,
    onOpenMovie: (FavoriteMovie, String) -> Unit,
    viewModel: FavoritesViewModel = viewModel(factory = viewModelFactory { FavoritesViewModel() }),
) {
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()

    if (favorites.isEmpty()) {
        Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            Text(
                "No favorites yet. Tap the heart on any poster to save it here.",
                color = Mist,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        return
    }

    Column(Modifier.fillMaxSize()) {
        Text(
            "Favorites",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp),
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(favorites, key = { (if (it.isTv) "tv" else "mv") + it.id }) { fav ->
                val key = "fav-${if (fav.isTv) "tv" else "mv"}-${fav.id}"
                with(sharedScope) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .aspectRatio(2f / 3f)
                            .clip(RoundedCornerShape(14.dp))
                            .sharedElement(
                                rememberSharedContentState(key = key),
                                animatedVisibilityScope = animatedScope,
                            )
                            .clickable { onOpenMovie(fav, key) }
                    ) {
                        ShimmerBox(Modifier.fillMaxSize())
                        AsyncImage(
                            model = ImageUrl.tmdbThumb(fav.posterPath) ?: fav.posterUrl,
                            contentDescription = fav.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }
        }
    }
}
