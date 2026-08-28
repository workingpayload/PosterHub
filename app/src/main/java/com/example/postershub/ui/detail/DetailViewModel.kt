package com.example.postershub.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.postershub.data.ImageUrl
import com.example.postershub.data.local.FavoriteMovie
import com.example.postershub.data.repository.MovieRepository
import com.example.postershub.data.repository.PosterRepository
import com.example.postershub.di.ServiceLocator
import com.example.postershub.domain.model.Movie
import com.example.postershub.domain.model.PosterImage
import com.example.postershub.util.classify
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DetailUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val movie: Movie? = null,
    val posters: List<PosterImage> = emptyList(),
)

class DetailViewModel(
    private val movieId: Int,
    private val isTv: Boolean,
    private val posterPath: String?,
    private val movieRepo: MovieRepository = ServiceLocator.movieRepository,
    private val posterRepo: PosterRepository = ServiceLocator.posterRepository,
    private val favorites: com.example.postershub.data.local.FavoritesStore = ServiceLocator.favoritesStore,
) : ViewModel() {

    private val mediaType = if (isTv) com.example.postershub.domain.model.MediaType.TV
    else com.example.postershub.domain.model.MediaType.MOVIE

    private val _state = MutableStateFlow(DetailUiState())
    val state: StateFlow<DetailUiState> = _state.asStateFlow()

    val isFavorite: StateFlow<Boolean> = favorites.favorites
        .map { list -> list.any { it.id == movieId } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    init {
        load()
    }

    fun load() {
        _state.value = _state.value.copy(loading = true, error = null)
        viewModelScope.launch {
            runCatching {
                coroutineScope {
                    val movie = async { movieRepo.details(movieId, mediaType) }
                    val posters = async { posterRepo.postersFor(movieId, mediaType, posterPath) }
                    DetailUiState(loading = false, movie = movie.await(), posters = posters.await())
                }
            }.onSuccess { _state.value = it }
                .onFailure {
                    _state.value = _state.value.copy(loading = false, error = it.classify().message)
                }
        }
    }

    fun toggleFavorite(fallbackTitle: String, fallbackPosterPath: String?) {
        viewModelScope.launch {
            val s = _state.value
            val posterUrl = s.posters.firstOrNull()?.url
                ?: ImageUrl.tmdbOriginal(s.movie?.posterPath ?: fallbackPosterPath)
                ?: return@launch
            favorites.toggle(
                FavoriteMovie(
                    id = movieId,
                    title = s.movie?.title ?: fallbackTitle,
                    posterPath = s.movie?.posterPath ?: fallbackPosterPath,
                    posterUrl = posterUrl,
                    addedAt = System.currentTimeMillis(),
                    isTv = isTv,
                )
            )
        }
    }
}
