package com.example.postershub.data.remote

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Adds auth per-host:
 *  - api.themoviedb.org: Bearer header for a v4 read token (starts with "eyJ"), else ?api_key= for a v3 key.
 *  - webservice.fanart.tv: ?api_key= query param.
 * Image hosts (image.tmdb.org, assets.fanart.tv) need no auth.
 */
class ApiAuthInterceptor(
    private val tmdbKey: String,
    private val fanartKey: String,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val host = request.url.host
        val builder = request.newBuilder()
        var url = request.url

        when {
            host.contains("themoviedb.org") -> {
                if (tmdbKey.startsWith("eyJ")) {
                    builder.addHeader("Authorization", "Bearer $tmdbKey")
                } else if (tmdbKey.isNotBlank()) {
                    url = url.newBuilder().addQueryParameter("api_key", tmdbKey).build()
                }
            }

            host.contains("fanart.tv") -> {
                if (fanartKey.isNotBlank()) {
                    url = url.newBuilder().addQueryParameter("api_key", fanartKey).build()
                }
            }
        }

        return chain.proceed(builder.url(url).build())
    }
}
