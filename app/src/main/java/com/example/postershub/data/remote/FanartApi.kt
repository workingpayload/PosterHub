package com.example.postershub.data.remote

import com.example.postershub.data.remote.dto.FanartMovieDto
import com.example.postershub.data.remote.dto.FanartTvDto
import retrofit2.http.GET
import retrofit2.http.Path

interface FanartApi {
    /** id = TMDB id or IMDb id. api_key added by the auth interceptor. */
    @GET("movies/{id}")
    suspend fun movie(@Path("id") id: Int): FanartMovieDto

    /** id must be a TheTVDB id (not TMDB). api_key added by the auth interceptor. */
    @GET("tv/{id}")
    suspend fun tv(@Path("id") tvdbId: Int): FanartTvDto
}
