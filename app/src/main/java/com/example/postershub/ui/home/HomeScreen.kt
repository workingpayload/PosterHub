package com.example.postershub.ui.home

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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

private const val ROW_HEIGHT_DP = 195

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    sharedScope: SharedTransitionScope,
    animatedScope: AnimatedVisibilityScope,
    onOpenMovie: (Movie, String) -> Unit,
    onOpenAbout: () -> Unit,
    viewModel: HomeViewModel = viewModel(factory = viewModelFactory { HomeViewModel() }),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    PullToRefreshBox(
        isRefreshing = state.refreshing,
        onRefresh = viewModel::loadAll,
        modifier = Modifier.fillMaxSize(),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            item {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, top = 16.dp, end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            "PostersHub",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                        )
                        Text("4K movie & TV posters", color = Mist, style = MaterialTheme.typography.bodyMedium)
                    }
                    IconButton(onClick = onOpenAbout) {
                        Icon(Icons.Filled.Info, contentDescription = "About")
                    }
                }
            }
            item {
                with(sharedScope) {
                    CarouselSection(
                        sectionState = state.trending,
                        animatedScope = animatedScope,
                        onClick = onOpenMovie,
                        onRetry = { viewModel.load(HomeSection.TRENDING) },
                    )
                }
            }
            posterRow("Popular", state.popular, "popular", sharedScope, animatedScope, onOpenMovie) {
                viewModel.load(HomeSection.POPULAR)
            }
            posterRow("Top Rated", state.topRated, "top", sharedScope, animatedScope, onOpenMovie) {
                viewModel.load(HomeSection.TOP_RATED)
            }
            posterRow("Now Playing", state.nowPlaying, "now", sharedScope, animatedScope, onOpenMovie) {
                viewModel.load(HomeSection.NOW_PLAYING)
            }
            posterRow("Trending Series", state.trendingTv, "tvtrend", sharedScope, animatedScope, onOpenMovie) {
                viewModel.load(HomeSection.TRENDING_TV)
            }
            posterRow("Popular Series", state.popularTv, "tvpop", sharedScope, animatedScope, onOpenMovie) {
                viewModel.load(HomeSection.POPULAR_TV)
            }
            posterRow("Top Rated Series", state.topRatedTv, "tvtop", sharedScope, animatedScope, onOpenMovie) {
                viewModel.load(HomeSection.TOP_RATED_TV)
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedTransitionScope.CarouselSection(
    sectionState: SectionState<List<Movie>>,
    animatedScope: AnimatedVisibilityScope,
    onClick: (Movie, String) -> Unit,
    onRetry: () -> Unit,
) {
    when (sectionState) {
        is SectionState.Loading -> ShimmerBox(Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(430.dp))
        is SectionState.Error -> RowErrorState(sectionState.message, onRetry, height = 430)
        is SectionState.Success -> if (sectionState.data.isNotEmpty()) {
            DepthCarousel(
                movies = sectionState.data,
                animatedScope = animatedScope,
                onClick = onClick,
                modifier = Modifier.fillMaxWidth().height(430.dp),
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
private fun LazyListScope.posterRow(
    title: String,
    sectionState: SectionState<List<Movie>>,
    keyPrefix: String,
    sharedScope: SharedTransitionScope,
    animatedScope: AnimatedVisibilityScope,
    onOpenMovie: (Movie, String) -> Unit,
    onRetry: () -> Unit,
) {
    if (sectionState is SectionState.Success && sectionState.data.isEmpty()) return

    item(key = "header-$keyPrefix") {
        Text(
            title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 16.dp),
        )
    }
    item(key = "row-$keyPrefix") {
        when (sectionState) {
            is SectionState.Loading -> ShimmerRow()
            is SectionState.Error -> RowErrorState(sectionState.message, onRetry, height = ROW_HEIGHT_DP)
            is SectionState.Success -> LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(sectionState.data, key = { it.id }) { movie ->
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
}

@Composable
private fun ShimmerRow() {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(6) {
            ShimmerBox(Modifier.width(130.dp).height(ROW_HEIGHT_DP.dp))
        }
    }
}

/** Compact inline error for a single row/section — doesn't blank out the rest of the feed. */
@Composable
private fun RowErrorState(message: String, onRetry: () -> Unit, height: Int) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(height.dp)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(message, color = Mist, style = MaterialTheme.typography.bodyMedium)
            TextButton(onClick = onRetry) { Text("Retry") }
        }
    }
}
