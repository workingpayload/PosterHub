package com.example.postershub.data.local

import android.content.Context
import android.graphics.Bitmap
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.postershub.util.ImageActions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream

/**
 * A saved poster/movie. [posterUrl] is the chosen full-res image at save time; [localPosterPath]
 * is a device-local copy so the Favorites grid still renders when offline (the remote URL may no
 * longer be in the image loader's disk cache).
 */
@Serializable
data class FavoriteMovie(
    val id: Int,
    val title: String,
    val posterPath: String?,
    val posterUrl: String,
    val addedAt: Long,
    val isTv: Boolean = false,
    val localPosterPath: String? = null,
    val voteAverage: Double = 0.0,
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
            val existing = current.firstOrNull { it.id == movie.id }
            val next = if (existing != null) {
                existing.localPosterPath?.let { File(it).delete() }
                current.filterNot { it.id == movie.id }
            } else {
                current + movie.copy(localPosterPath = cachePoster(movie.id, movie.posterUrl))
            }
            prefs[key] = json.encodeToString(next)
        }
    }

    /** Downloads and stores a local JPEG copy of the poster so Favorites works offline. */
    private suspend fun cachePoster(id: Int, url: String): String? = withContext(Dispatchers.IO) {
        runCatching {
            val bmp = ImageActions.loadBitmap(context, url) ?: return@runCatching null
            val dir = File(context.filesDir, "favorite_posters").apply { mkdirs() }
            val file = File(dir, "$id.jpg")
            FileOutputStream(file).use { out -> bmp.compress(Bitmap.CompressFormat.JPEG, 85, out) }
            file.absolutePath
        }.getOrNull()
    }
}
