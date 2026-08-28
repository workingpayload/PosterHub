package com.example.postershub.data

/** Builds TMDB image URLs. See https://developer.themoviedb.org/docs/image-basics */
object ImageUrl {
    private const val BASE = "https://image.tmdb.org/t/p/"

    /** size e.g. "w185", "w342", "w500", "w780", "original". Null path -> null. */
    fun tmdb(path: String?, size: String): String? =
        path?.let { BASE + size + it }

    fun tmdbOriginal(path: String?): String? = tmdb(path, "original")

    /** Good grid thumbnail size. */
    fun tmdbThumb(path: String?): String? = tmdb(path, "w342")

    /** Wide backdrop for detail headers. */
    fun tmdbBackdrop(path: String?): String? = tmdb(path, "w1280")
}
