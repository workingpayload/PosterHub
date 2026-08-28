package com.example.postershub.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.postershub.data.local.FavoriteMovie
import com.example.postershub.data.local.FavoritesStore
import com.example.postershub.di.ServiceLocator
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class FavoritesViewModel(
    store: FavoritesStore = ServiceLocator.favoritesStore,
) : ViewModel() {
    val favorites: StateFlow<List<FavoriteMovie>> = store.favorites
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
