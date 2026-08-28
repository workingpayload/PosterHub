package com.example.postershub.ui.favorites

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import java.io.File

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    sharedScope: SharedTransitionScope,
    animatedScope: AnimatedVisibilityScope,
    onOpenMovie: (FavoriteMovie, String) -> Unit,
    onGoToSearch: () -> Unit,
    scrollToTopSignal: Int = 0,
    viewModel: FavoritesViewModel = viewModel(factory = viewModelFactory { FavoritesViewModel() }),
) {
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    val gridState = rememberLazyGridState()

    LaunchedEffect(scrollToTopSignal) {
        if (scrollToTopSignal > 0) gridState.animateScrollToItem(0)
    }

    if (favorites.isEmpty()) {
        Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    "No favorites yet. Tap the heart on any poster to save it here.",
                    color = Mist,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(horizontal = 8.dp),
                )
                Button(onClick = onGoToSearch) { Text("Browse & Search") }
            }
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
            state = gridState,
            columns = GridCells.Adaptive(minSize = 110.dp),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(favorites, key = { (if (it.isTv) "tv" else "mv") + it.id }) { fav ->
                val key = "fav-${if (fav.isTv) "tv" else "mv"}-${fav.id}"
                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = { value ->
                        if (value != SwipeToDismissBoxValue.Settled) viewModel.remove(fav)
                        true
                    },
                )
                SwipeToDismissBox(
                    state = dismissState,
                    backgroundContent = {
                        Box(
                            Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.error),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Filled.Delete, contentDescription = "Remove", tint = Color.White)
                        }
                    },
                ) {
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
                                model = fav.localPosterPath?.let(::File)
                                    ?: (ImageUrl.tmdbThumb(fav.posterPath) ?: fav.posterUrl),
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
}
