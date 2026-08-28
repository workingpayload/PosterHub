package com.example.postershub.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ---------- TMDB ----------

@Serializable
data class TmdbPageDto(
    val page: Int = 1,
    val results: List<TmdbMovieDto> = emptyList(),
    @SerialName("total_pages") val totalPages: Int = 1,
)

@Serializable
data class TmdbMovieDto(
    val id: Int,
    val title: String? = null,
    val name: String? = null, // TV endpoints use "name"
    val overview: String = "",
    @SerialName("poster_path") val posterPath: String? = null,
    @SerialName("backdrop_path") val backdropPath: String? = null,
    @SerialName("release_date") val releaseDate: String? = null,
    @SerialName("first_air_date") val firstAirDate: String? = null, // TV
    @SerialName("vote_average") val voteAverage: Double = 0.0,
    @SerialName("media_type") val mediaType: String? = null, // present in /trending/all and /search/multi
)

@Serializable
data class TmdbExternalIdsDto(
    @SerialName("tvdb_id") val tvdbId: Int? = null,
    @SerialName("imdb_id") val imdbId: String? = null,
)

@Serializable
data class TmdbImagesDto(
    val posters: List<TmdbImageDto> = emptyList(),
    val backdrops: List<TmdbImageDto> = emptyList(),
)

@Serializable
data class TmdbImageDto(
    @SerialName("file_path") val filePath: String,
    val width: Int = 0,
    val height: Int = 0,
    @SerialName("iso_639_1") val language: String? = null,
    @SerialName("vote_average") val voteAverage: Double = 0.0,
)

// ---------- fanart.tv ----------

@Serializable
data class FanartMovieDto(
    val movieposter: List<FanartImageDto> = emptyList(),
)

@Serializable
data class FanartTvDto(
    val tvposter: List<FanartImageDto> = emptyList(),
)

@Serializable
data class FanartImageDto(
    val id: String? = null,
    val url: String,
    val lang: String? = null,
    val likes: String? = null,
)
