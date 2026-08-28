package com.example.postershub.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.postershub.data.local.FavoriteMovie
import com.example.postershub.data.local.FavoritesStore
import com.example.postershub.di.ServiceLocator
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FavoritesViewModel(
    private val store: FavoritesStore = ServiceLocator.favoritesStore,
) : ViewModel() {
    val favorites: StateFlow<List<FavoriteMovie>> = store.favorites
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** [favorite] is already in the list, so this always removes it (toggle is add-or-remove). */
    fun remove(favorite: FavoriteMovie) {
        viewModelScope.launch { store.toggle(favorite) }
    }
}
