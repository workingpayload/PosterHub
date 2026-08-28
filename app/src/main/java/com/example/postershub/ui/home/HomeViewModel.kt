package com.example.postershub.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.postershub.data.repository.MovieRepository
import com.example.postershub.di.ServiceLocator
import com.example.postershub.domain.model.Movie
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val trending: List<Movie> = emptyList(),
    val popular: List<Movie> = emptyList(),
    val topRated: List<Movie> = emptyList(),
    val nowPlaying: List<Movie> = emptyList(),
    val trendingTv: List<Movie> = emptyList(),
    val popularTv: List<Movie> = emptyList(),
    val topRatedTv: List<Movie> = emptyList(),
)

class HomeViewModel(
    private val repo: MovieRepository = ServiceLocator.movieRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        load()
    }

    private fun List<Movie>.withPoster() = filter { it.posterPath != null }

    fun load() {
        _state.value = _state.value.copy(loading = true, error = null)
        viewModelScope.launch {
            runCatching {
                coroutineScope {
                    val trending = async { repo.trending() }
                    val popular = async { repo.popular() }
                    val topRated = async { repo.topRated() }
                    val nowPlaying = async { repo.nowPlaying() }
                    val trendingTv = async { repo.trendingTv() }
                    val popularTv = async { repo.popularTv() }
                    val topRatedTv = async { repo.topRatedTv() }
                    HomeUiState(
                        loading = false,
                        trending = trending.await().withPoster(),
                        popular = popular.await().withPoster(),
                        topRated = topRated.await().withPoster(),
                        nowPlaying = nowPlaying.await().withPoster(),
                        trendingTv = trendingTv.await().withPoster(),
                        popularTv = popularTv.await().withPoster(),
                        topRatedTv = topRatedTv.await().withPoster(),
                    )
                }
            }.onSuccess { _state.value = it }
                .onFailure {
                    _state.value = _state.value.copy(
                        loading = false,
                        error = it.message ?: "Failed to load. Check your TMDB key and connection.",
                    )
                }
        }
    }
}
