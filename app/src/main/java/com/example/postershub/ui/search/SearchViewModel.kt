package com.example.postershub.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import com.example.postershub.data.repository.MovieRepository
import com.example.postershub.di.ServiceLocator
import com.example.postershub.domain.model.Movie
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

enum class MediaFilter { ALL, MOVIE, TV }

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class SearchViewModel(
    private val repo: MovieRepository = ServiceLocator.movieRepository,
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _mediaFilter = MutableStateFlow(MediaFilter.ALL)
    val mediaFilter: StateFlow<MediaFilter> = _mediaFilter.asStateFlow()

    val results: Flow<PagingData<Movie>> = combine(
        _query.debounce(350).map { it.trim() }.distinctUntilChanged(),
        _mediaFilter,
    ) { q, filter -> q to filter }
        .flatMapLatest { (q, filter) ->
            if (q.length < 2) flowOf(PagingData.empty())
            else repo.search(q).map { page ->
                page.filter { movie ->
                    when (filter) {
                        MediaFilter.ALL -> true
                        MediaFilter.MOVIE -> !movie.isTv
                        MediaFilter.TV -> movie.isTv
                    }
                }
            }
        }
        .cachedIn(viewModelScope)

    fun onQueryChange(value: String) {
        _query.value = value
    }

    fun onMediaFilterChange(value: MediaFilter) {
        _mediaFilter.value = value
    }
}
