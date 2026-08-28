package com.example.postershub.data.remote

import com.example.postershub.data.remote.dto.TmdbExternalIdsDto
import com.example.postershub.data.remote.dto.TmdbImagesDto
import com.example.postershub.data.remote.dto.TmdbMovieDto
import com.example.postershub.data.remote.dto.TmdbPageDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TmdbApi {

    // ---------- Movies ----------
    @GET("trending/movie/week")
    suspend fun trending(@Query("page") page: Int = 1): TmdbPageDto

    @GET("movie/popular")
    suspend fun popular(@Query("page") page: Int = 1): TmdbPageDto

    @GET("movie/top_rated")
    suspend fun topRated(@Query("page") page: Int = 1): TmdbPageDto

    @GET("movie/now_playing")
    suspend fun nowPlaying(@Query("page") page: Int = 1): TmdbPageDto

    @GET("movie/{id}")
    suspend fun details(@Path("id") id: Int): TmdbMovieDto

    @GET("movie/{id}/images")
    suspend fun images(
        @Path("id") id: Int,
        @Query("include_image_language") includeImageLanguage: String = "en,null",
    ): TmdbImagesDto

    // ---------- TV ----------
    @GET("trending/tv/week")
    suspend fun trendingTv(@Query("page") page: Int = 1): TmdbPageDto

    @GET("tv/popular")
    suspend fun popularTv(@Query("page") page: Int = 1): TmdbPageDto

    @GET("tv/top_rated")
    suspend fun topRatedTv(@Query("page") page: Int = 1): TmdbPageDto

    @GET("tv/on_the_air")
    suspend fun onTheAirTv(@Query("page") page: Int = 1): TmdbPageDto

    @GET("tv/{id}")
    suspend fun tvDetails(@Path("id") id: Int): TmdbMovieDto

    @GET("tv/{id}/images")
    suspend fun tvImages(
        @Path("id") id: Int,
        @Query("include_image_language") includeImageLanguage: String = "en,null",
    ): TmdbImagesDto

    @GET("tv/{id}/external_ids")
    suspend fun tvExternalIds(@Path("id") id: Int): TmdbExternalIdsDto

    // ---------- Search (movies + TV) ----------
    @GET("search/multi")
    suspend fun searchMulti(
        @Query("query") query: String,
        @Query("page") page: Int = 1,
        @Query("include_adult") includeAdult: Boolean = false,
    ): TmdbPageDto
}
