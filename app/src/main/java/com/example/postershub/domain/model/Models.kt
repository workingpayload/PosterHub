package com.example.postershub.domain.model

enum class MediaType { MOVIE, TV }

/** A cast member as shown on the detail screen. */
data class CastMember(
    val id: Int,
    val name: String,
    val character: String?,
    val profilePath: String?,
)

/** A movie or TV series as shown in feeds / search / detail. Paths are raw TMDB paths. */
data class Movie(
    val id: Int,
    val title: String,
    val overview: String,
    val posterPath: String?,
    val backdropPath: String?,
    val releaseDate: String?,
    val voteAverage: Double,
    val mediaType: MediaType = MediaType.MOVIE,
    val genres: List<String> = emptyList(),
    val runtimeMinutes: Int? = null,
    val cast: List<CastMember> = emptyList(),
    val similar: List<Movie> = emptyList(),
) {
    val year: String? get() = releaseDate?.takeIf { it.length >= 4 }?.substring(0, 4)
    val isTv: Boolean get() = mediaType == MediaType.TV
}

enum class ImageSource { TMDB, FANART }

/** A single poster candidate for a title, from any source, ready to display/download. */
data class PosterImage(
    val url: String,        // full-resolution URL
    val thumbUrl: String,   // smaller URL for grids/strips
    val width: Int,
    val height: Int,
    val source: ImageSource,
    val language: String?,  // ISO-639-1, "" / null => textless
) {
    val pixels: Long get() = width.toLong() * height.toLong()
    val isTextless: Boolean get() = language.isNullOrBlank() || language == "00"

    /** TMDB "original" posters are ~2000px wide; fanart.tv posters are ~1000px and never qualify. */
    val isUltraHd: Boolean get() = width >= 1800
}
