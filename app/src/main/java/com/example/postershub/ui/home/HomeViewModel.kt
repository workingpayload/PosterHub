package com.example.postershub.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.postershub.data.repository.MovieRepository
import com.example.postershub.di.ServiceLocator
import com.example.postershub.domain.model.Movie
import com.example.postershub.util.ErrorKind
import com.example.postershub.util.classify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Each home row loads and fails independently, so one flaky endpoint doesn't blank the feed. */
sealed interface SectionState<out T> {
    data object Loading : SectionState<Nothing>
    data class Success<T>(val data: T) : SectionState<T>
    data class Error(val message: String, val kind: ErrorKind) : SectionState<Nothing>
}

enum class HomeSection { TRENDING, POPULAR, TOP_RATED, NOW_PLAYING, TRENDING_TV, POPULAR_TV, TOP_RATED_TV }

data class HomeUiState(
    val trending: SectionState<List<Movie>> = SectionState.Loading,
    val popular: SectionState<List<Movie>> = SectionState.Loading,
    val topRated: SectionState<List<Movie>> = SectionState.Loading,
    val nowPlaying: SectionState<List<Movie>> = SectionState.Loading,
    val trendingTv: SectionState<List<Movie>> = SectionState.Loading,
    val popularTv: SectionState<List<Movie>> = SectionState.Loading,
    val topRatedTv: SectionState<List<Movie>> = SectionState.Loading,
)

class HomeViewModel(
    private val repo: MovieRepository = ServiceLocator.movieRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        loadAll()
    }

    fun loadAll() {
        HomeSection.entries.forEach(::load)
    }

    fun load(section: HomeSection) {
        viewModelScope.launch {
            updateSection(section, SectionState.Loading)
            val result = runCatching { fetch(section).filter { it.posterPath != null } }
            val next = result.fold(
                onSuccess = { SectionState.Success(it) },
                onFailure = { val e = it.classify(); SectionState.Error(e.message, e.kind) },
            )
            updateSection(section, next)
        }
    }

    private suspend fun fetch(section: HomeSection): List<Movie> = when (section) {
        HomeSection.TRENDING -> repo.trending()
        HomeSection.POPULAR -> repo.popular()
        HomeSection.TOP_RATED -> repo.topRated()
        HomeSection.NOW_PLAYING -> repo.nowPlaying()
        HomeSection.TRENDING_TV -> repo.trendingTv()
        HomeSection.POPULAR_TV -> repo.popularTv()
        HomeSection.TOP_RATED_TV -> repo.topRatedTv()
    }

    private fun updateSection(section: HomeSection, newState: SectionState<List<Movie>>) {
        _state.update { current ->
            when (section) {
                HomeSection.TRENDING -> current.copy(trending = newState)
                HomeSection.POPULAR -> current.copy(popular = newState)
                HomeSection.TOP_RATED -> current.copy(topRated = newState)
                HomeSection.NOW_PLAYING -> current.copy(nowPlaying = newState)
                HomeSection.TRENDING_TV -> current.copy(trendingTv = newState)
                HomeSection.POPULAR_TV -> current.copy(popularTv = newState)
                HomeSection.TOP_RATED_TV -> current.copy(topRatedTv = newState)
            }
        }
    }
}
