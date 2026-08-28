package com.example.postershub.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.postershub.data.local.FavoriteMovie
import com.example.postershub.data.local.FavoritesStore
import com.example.postershub.di.ServiceLocator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class FavoritesSort { RECENT, TITLE, RATING }

class FavoritesViewModel(
    private val store: FavoritesStore = ServiceLocator.favoritesStore,
) : ViewModel() {

    private val _sort = MutableStateFlow(FavoritesSort.RECENT)
    val sort: StateFlow<FavoritesSort> = _sort.asStateFlow()

    val favorites: StateFlow<List<FavoriteMovie>> = combine(store.favorites, _sort) { list, sort ->
        when (sort) {
            FavoritesSort.RECENT -> list // FavoritesStore already sorts by addedAt desc
            FavoritesSort.TITLE -> list.sortedBy { it.title.lowercase() }
            FavoritesSort.RATING -> list.sortedByDescending { it.voteAverage }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSort(value: FavoritesSort) {
        _sort.value = value
    }

    /** [favorite] is already in the list, so this always removes it (toggle is add-or-remove). */
    fun remove(favorite: FavoriteMovie) {
        viewModelScope.launch { store.toggle(favorite) }
    }
}
