package com.example.postershub.data.repository

import com.example.postershub.data.ImageUrl
import com.example.postershub.data.remote.FanartApi
import com.example.postershub.data.remote.TmdbApi
import com.example.postershub.data.remote.dto.TmdbImageDto
import com.example.postershub.domain.model.ImageSource
import com.example.postershub.domain.model.MediaType
import com.example.postershub.domain.model.PosterImage
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

/**
 * Merges every available poster for a title (movie or TV) from TMDB + fanart.tv, ranks by pixel
 * count (highest-res / "4K-HD" first), and pins the title's default poster to the front.
 *
 * Results are cached in memory keyed by (id, type, preferredPath) so that the detail screen and
 * the fullscreen viewer see the EXACT same ordered list — otherwise a re-fetch could reorder tied
 * variants and a tapped index would open a different image.
 */
class PosterRepository(
    private val tmdb: TmdbApi,
    private val fanart: FanartApi,
) {
    // fanart.tv doesn't report dimensions; poster assets are consistently ~1000x1426.
    private val fanartNominalWidth = 1000
    private val fanartNominalHeight = 1426

    private val cache = LinkedHashMap<String, List<PosterImage>>()
    private val maxCacheEntries = 40

    suspend fun postersFor(
        id: Int,
        type: MediaType,
        preferredPath: String? = null,
    ): List<PosterImage> = coroutineScope {
        val key = "$id|$type|$preferredPath"
        synchronized(cache) { cache[key] }?.let { return@coroutineScope it }

        val tmdbDeferred = async {
            runCatching {
                if (type == MediaType.TV) tmdb.tvImages(id).posters else tmdb.images(id).posters
            }.getOrDefault(emptyList())
        }
        val fanartDeferred = async {
            runCatching {
                if (type == MediaType.TV) {
                    val tvdbId = tmdb.tvExternalIds(id).tvdbId
                    if (tvdbId != null) fanart.tv(tvdbId).tvposter else emptyList()
                } else {
                    fanart.movie(id).movieposter
                }
            }.getOrDefault(emptyList())
        }

        val tmdbPosters = tmdbDeferred.await().mapNotNull { dto: TmdbImageDto ->
            val full = ImageUrl.tmdbOriginal(dto.filePath) ?: return@mapNotNull null
            PosterImage(
                url = full,
                thumbUrl = ImageUrl.tmdbThumb(dto.filePath) ?: full,
                width = dto.width,
                height = dto.height,
                source = ImageSource.TMDB,
                language = dto.language,
            )
        }

        val fanartPosters = fanartDeferred.await().map { dto ->
            PosterImage(
                url = dto.url,
                thumbUrl = dto.url,
                width = fanartNominalWidth,
                height = fanartNominalHeight,
                source = ImageSource.FANART,
                language = dto.lang,
            )
        }

        val ranked = (tmdbPosters + fanartPosters)
            .distinctBy { it.url }
            .sortedWith(
                compareByDescending<PosterImage> { it.pixels }
                    .thenBy { it.source == ImageSource.FANART }
                    .thenByDescending { it.isTextless }
            )

        val result = run {
            val preferredUrl = ImageUrl.tmdbOriginal(preferredPath) ?: return@run ranked
            val primary = ranked.firstOrNull { it.url == preferredUrl } ?: PosterImage(
                url = preferredUrl,
                thumbUrl = ImageUrl.tmdbThumb(preferredPath) ?: preferredUrl,
                width = 2000,
                height = 3000,
                source = ImageSource.TMDB,
                language = null,
            )
            listOf(primary) + ranked.filterNot { it.url == primary.url }
        }

        if (result.isNotEmpty()) {
            synchronized(cache) {
                cache[key] = result
                while (cache.size > maxCacheEntries) {
                    cache.remove(cache.keys.first())
                }
            }
        }
        result
    }
}
