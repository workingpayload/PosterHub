package com.example.postershub.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/** A saved poster/movie. posterUrl is the chosen full-res image at save time. */
@Serializable
data class FavoriteMovie(
    val id: Int,
    val title: String,
    val posterPath: String?,
    val posterUrl: String,
    val addedAt: Long,
    val isTv: Boolean = false,
)

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "postershub_favorites")

/** JSON-backed favorites persistence. No annotation processor / codegen required. */
class FavoritesStore(private val context: Context) {

    private val key = stringPreferencesKey("favorites_json")
    private val json = Json { ignoreUnknownKeys = true }

    val favorites: Flow<List<FavoriteMovie>> = context.dataStore.data.map { prefs ->
        prefs[key]?.let { raw ->
            runCatching { json.decodeFromString<List<FavoriteMovie>>(raw) }.getOrDefault(emptyList())
        }?.sortedByDescending { it.addedAt } ?: emptyList()
    }

    suspend fun toggle(movie: FavoriteMovie) {
        context.dataStore.edit { prefs ->
            val current = prefs[key]
                ?.let { runCatching { json.decodeFromString<List<FavoriteMovie>>(it) }.getOrDefault(emptyList()) }
                ?: emptyList()
            val next = if (current.any { it.id == movie.id }) {
                current.filterNot { it.id == movie.id }
            } else {
                current + movie
            }
            prefs[key] = json.encodeToString(next)
        }
    }
}
