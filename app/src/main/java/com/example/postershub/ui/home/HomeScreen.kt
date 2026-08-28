package com.example.postershub.ui.home

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.postershub.domain.model.Movie
import com.example.postershub.ui.components.DepthCarousel
import com.example.postershub.ui.components.PosterCard
import com.example.postershub.ui.components.ShimmerBox
import com.example.postershub.ui.theme.Mist
import com.example.postershub.util.viewModelFactory

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun HomeScreen(
    sharedScope: SharedTransitionScope,
    animatedScope: AnimatedVisibilityScope,
    onOpenMovie: (Movie, String) -> Unit,
    viewModel: HomeViewModel = viewModel(factory = viewModelFactory { HomeViewModel() }),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    when {
        state.loading -> HomeSkeleton()
        state.error != null -> ErrorState(message = state.error!!, onRetry = viewModel::load)
        else -> LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            item {
                Column(Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp)) {
                    Text(
                        "PostersHub",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                    )
                    Text("4K movie & TV posters", color = Mist, style = MaterialTheme.typography.bodyMedium)
                }
            }
            item {
                with(sharedScope) {
                    DepthCarousel(
                        movies = state.trending,
                        animatedScope = animatedScope,
                        onClick = onOpenMovie,
                        modifier = Modifier.fillMaxWidth().height(430.dp),
                    )
                }
            }
            posterRow("Popular", state.popular, "popular", sharedScope, animatedScope, onOpenMovie)
            posterRow("Top Rated", state.topRated, "top", sharedScope, animatedScope, onOpenMovie)
            posterRow("Now Playing", state.nowPlaying, "now", sharedScope, animatedScope, onOpenMovie)
            posterRow("Trending Series", state.trendingTv, "tvtrend", sharedScope, animatedScope, onOpenMovie)
            posterRow("Popular Series", state.popularTv, "tvpop", sharedScope, animatedScope, onOpenMovie)
            posterRow("Top Rated Series", state.topRatedTv, "tvtop", sharedScope, animatedScope, onOpenMovie)
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
private fun androidx.compose.foundation.lazy.LazyListScope.posterRow(
    title: String,
    movies: List<Movie>,
    keyPrefix: String,
    sharedScope: SharedTransitionScope,
    animatedScope: AnimatedVisibilityScope,
    onOpenMovie: (Movie, String) -> Unit,
) {
    if (movies.isEmpty()) return
    item(key = "header-$keyPrefix") {
        Text(
            title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 16.dp),
        )
    }
    item(key = "row-$keyPrefix") {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(movies, key = { it.id }) { movie ->
                val key = "$keyPrefix-${movie.id}"
                with(sharedScope) {
                    PosterCard(
                        movie = movie,
                        sharedKey = key,
                        animatedScope = animatedScope,
                        onClick = { onOpenMovie(movie, key) },
                        modifier = Modifier.width(130.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeSkeleton() {
    Column(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ShimmerBox(Modifier.fillMaxWidth().height(420.dp))
        repeat(2) {
            ShimmerBox(Modifier.width(160.dp).height(24.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(6) {
                    ShimmerBox(Modifier.width(130.dp).height(195.dp))
                }
            }
        }
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(message, color = Mist)
            Button(onClick = onRetry) { Text("Retry") }
        }
    }
}
