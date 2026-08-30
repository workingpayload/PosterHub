package com.example.postershub.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.postershub.data.remote.TmdbApi
import com.example.postershub.data.remote.dto.TmdbMovieDto
import com.example.postershub.data.remote.dto.TmdbPageDto
import com.example.postershub.domain.model.CastMember
import com.example.postershub.domain.model.MediaType
import com.example.postershub.domain.model.Movie
import kotlinx.coroutines.flow.Flow

fun TmdbMovieDto.toMovie(default: MediaType = MediaType.MOVIE): Movie {
    val type = when (mediaType) {
        "tv" -> MediaType.TV
        "movie" -> MediaType.MOVIE
        else -> default
    }
    return Movie(
        id = id,
        title = title ?: name ?: "Untitled",
        overview = overview,
        posterPath = posterPath,
        backdropPath = backdropPath,
        releaseDate = releaseDate ?: firstAirDate,
        voteAverage = voteAverage,
        mediaType = type,
        genres = genres.map { it.name },
        runtimeMinutes = runtime ?: episodeRunTime.firstOrNull(),
        cast = credits?.cast.orEmpty().take(15).map { CastMember(it.id, it.name, it.character, it.profilePath) },
        similar = similar?.results.orEmpty().map { it.toMovie(type) },
    )
}

class MovieRepository(private val api: TmdbApi) {

    // Movies
    suspend fun trending(): List<Movie> = api.trending().results.map { it.toMovie(MediaType.MOVIE) }
    suspend fun popular(): List<Movie> = api.popular().results.map { it.toMovie(MediaType.MOVIE) }
    suspend fun topRated(): List<Movie> = api.topRated().results.map { it.toMovie(MediaType.MOVIE) }
    suspend fun nowPlaying(): List<Movie> = api.nowPlaying().results.map { it.toMovie(MediaType.MOVIE) }

    // TV
    suspend fun trendingTv(): List<Movie> = api.trendingTv().results.map { it.toMovie(MediaType.TV) }
    suspend fun popularTv(): List<Movie> = api.popularTv().results.map { it.toMovie(MediaType.TV) }
    suspend fun topRatedTv(): List<Movie> = api.topRatedTv().results.map { it.toMovie(MediaType.TV) }
    suspend fun onTheAirTv(): List<Movie> = api.onTheAirTv().results.map { it.toMovie(MediaType.TV) }

    suspend fun details(id: Int, type: MediaType): Movie =
        if (type == MediaType.TV) api.tvDetails(id).toMovie(MediaType.TV)
        else api.details(id).toMovie(MediaType.MOVIE)

    /** Multi search: returns both movies and TV (persons filtered out). */
    fun search(query: String): Flow<PagingData<Movie>> =
        Pager(PagingConfig(pageSize = 20, initialLoadSize = 20)) {
            TmdbPagingSource { page -> api.searchMulti(query = query, page = page) }
        }.flow
}

class TmdbPagingSource(
    private val fetch: suspend (page: Int) -> TmdbPageDto,
) : PagingSource<Int, Movie>() {

    // TMDB search results can repeat the same item across adjacent pages (score ties at the
    // page boundary); LazyVerticalGrid requires unique keys, so dedupe across the whole session.
    private val seenIds = mutableSetOf<String>()

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Movie> = try {
        val page = params.key ?: 1
        val response = fetch(page)
        val movies = response.results
            .filter { it.mediaType != "person" && it.posterPath != null }
            .map { it.toMovie() }
            .filter { seenIds.add("${it.mediaType}-${it.id}") }
        LoadResult.Page(
            data = movies,
            prevKey = null,
            nextKey = if (page < response.totalPages) page + 1 else null,
        )
    } catch (t: Throwable) {
        LoadResult.Error(t)
    }

    override fun getRefreshKey(state: PagingState<Int, Movie>): Int? =
        state.anchorPosition?.let { anchor ->
            state.closestPageToPosition(anchor)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchor)?.nextKey?.minus(1)
        }
}
