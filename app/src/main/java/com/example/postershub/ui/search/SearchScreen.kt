package com.example.postershub.ui.search

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.example.postershub.domain.model.Movie
import com.example.postershub.ui.components.PosterCard
import com.example.postershub.ui.components.ShimmerBox
import com.example.postershub.ui.theme.Mist
import com.example.postershub.util.classify
import com.example.postershub.util.viewModelFactory

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    sharedScope: SharedTransitionScope,
    animatedScope: AnimatedVisibilityScope,
    onOpenMovie: (Movie, String) -> Unit,
    scrollToTopSignal: Int = 0,
    viewModel: SearchViewModel = viewModel(factory = viewModelFactory { SearchViewModel() }),
) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val mediaFilter by viewModel.mediaFilter.collectAsStateWithLifecycle()
    val recentQueries by viewModel.recentQueries.collectAsStateWithLifecycle()
    val results = viewModel.results.collectAsLazyPagingItems()
    val gridState = rememberLazyGridState()

    LaunchedEffect(scrollToTopSignal) {
        if (scrollToTopSignal > 0) gridState.animateScrollToItem(0)
    }

    Column(Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = query,
            onValueChange = viewModel::onQueryChange,
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { viewModel.onQueryChange("") }) {
                        Icon(Icons.Filled.Close, contentDescription = "Clear search")
                    }
                }
            },
            placeholder = { Text("Search movies") },
            singleLine = true,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = ImeAction.Search),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        ) {
            MediaFilter.entries.forEach { filter ->
                FilterChip(
                    selected = mediaFilter == filter,
                    onClick = { viewModel.onMediaFilterChange(filter) },
                    label = { Text(filter.label()) },
                )
            }
        }

        val refreshing = results.loadState.refresh is LoadState.Loading
        val error = results.loadState.refresh as? LoadState.Error

        when {
            query.trim().length < 2 -> if (recentQueries.isNotEmpty()) {
                RecentSearches(
                    queries = recentQueries,
                    onSelect = viewModel::onQueryChange,
                    onClear = viewModel::clearRecentQueries,
                )
            } else {
                CenterHint("Type at least 2 characters to search.")
            }
            refreshing -> SearchSkeleton()
            error != null -> CenterHint(error.error.classify().message, onRetry = results::retry)
            results.itemCount == 0 -> CenterHint("No results for \"$query\".")
            // isRefreshing is always false here: once results.refresh() flips loadState.refresh to
            // Loading, the `refreshing` branch above takes over and shows the skeleton instead.
            else -> PullToRefreshBox(
                isRefreshing = false,
                onRefresh = { results.refresh() },
                modifier = Modifier.fillMaxSize(),
            ) {
                LazyVerticalGrid(
                    state = gridState,
                    columns = GridCells.Adaptive(minSize = 110.dp),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(
                        count = results.itemCount,
                        key = results.itemKey { "${it.mediaType}-${it.id}" },
                    ) { index ->
                        val movie = results[index] ?: return@items
                        val key = "search-${movie.mediaType}-${movie.id}"
                        with(sharedScope) {
                            PosterCard(
                                movie = movie,
                                sharedKey = key,
                                animatedScope = animatedScope,
                                showTypeBadge = mediaFilter == MediaFilter.ALL,
                                onClick = { onOpenMovie(movie, key) },
                            )
                        }
                    }
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        AppendFooter(loadState = results.loadState.append, onRetry = results::retry)
                    }
                }
            }
        }
    }
}

private fun MediaFilter.label(): String = when (this) {
    MediaFilter.ALL -> "All"
    MediaFilter.MOVIE -> "Movies"
    MediaFilter.TV -> "TV"
}

@Composable
private fun AppendFooter(loadState: LoadState, onRetry: () -> Unit) {
    when (loadState) {
        is LoadState.Loading -> Box(
            Modifier.fillMaxWidth().padding(16.dp),
            contentAlignment = Alignment.Center,
        ) { CircularProgressIndicator() }
        is LoadState.Error -> Box(
            Modifier.fillMaxWidth().padding(16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(loadState.error.classify().message, color = Mist)
                TextButton(onClick = onRetry) { Text("Retry") }
            }
        }
        is LoadState.NotLoading -> Unit
    }
}

@Composable
private fun RecentSearches(queries: List<String>, onSelect: (String) -> Unit, onClear: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        ) {
            Text(
                "Recent",
                style = MaterialTheme.typography.labelLarge,
                color = Mist,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onClear) { Text("Clear") }
        }
        LazyColumn {
            items(queries) { q ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(q) }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                ) {
                    Icon(Icons.Filled.History, contentDescription = null, tint = Mist)
                    Text(q)
                }
            }
        }
    }
}

@Composable
private fun CenterHint(text: String, onRetry: (() -> Unit)? = null) {
    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text, color = Mist, style = MaterialTheme.typography.bodyLarge)
            onRetry?.let { TextButton(onClick = it) { Text("Retry") } }
        }
    }
}

@Composable
private fun SearchSkeleton() {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 110.dp),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        items(12) {
            ShimmerBox(Modifier.fillMaxWidth().aspectRatio(2f / 3f))
        }
    }
}
