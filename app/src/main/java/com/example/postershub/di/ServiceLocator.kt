package com.example.postershub.di

import android.content.Context
import com.example.postershub.BuildConfig
import com.example.postershub.data.local.FavoritesStore
import com.example.postershub.data.local.SettingsStore
import com.example.postershub.data.remote.ApiAuthInterceptor
import com.example.postershub.data.remote.FanartApi
import com.example.postershub.data.remote.TmdbApi
import com.example.postershub.data.repository.MovieRepository
import com.example.postershub.data.repository.PosterRepository
import kotlinx.serialization.json.Json
import okhttp3.Dns
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.dnsoverhttps.DnsOverHttps
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.net.InetAddress

/** Minimal manual dependency container, initialized once from the Application. */
object ServiceLocator {

    lateinit var okHttpClient: OkHttpClient
        private set
    lateinit var movieRepository: MovieRepository
        private set
    lateinit var posterRepository: PosterRepository
        private set
    lateinit var favoritesStore: FavoritesStore
        private set
    lateinit var settingsStore: SettingsStore
        private set

    private var initialized = false

    fun init(context: Context) {
        if (initialized) return
        initialized = true

        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }

        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC
            else HttpLoggingInterceptor.Level.NONE
        }

        okHttpClient = OkHttpClient.Builder()
            .dns(dnsOverHttps())
            .addInterceptor(ApiAuthInterceptor(BuildConfig.TMDB_API_KEY, BuildConfig.FANART_API_KEY))
            .addInterceptor(logging)
            .build()

        val contentType = "application/json".toMediaType()
        val converter = json.asConverterFactory(contentType)

        val tmdbApi = Retrofit.Builder()
            .baseUrl("https://api.themoviedb.org/3/")
            .client(okHttpClient)
            .addConverterFactory(converter)
            .build()
            .create(TmdbApi::class.java)

        val fanartApi = Retrofit.Builder()
            .baseUrl("https://webservice.fanart.tv/v3/")
            .client(okHttpClient)
            .addConverterFactory(converter)
            .build()
            .create(FanartApi::class.java)

        movieRepository = MovieRepository(tmdbApi)
        posterRepository = PosterRepository(tmdbApi, fanartApi)
        favoritesStore = FavoritesStore(context.applicationContext)
        settingsStore = SettingsStore(context.applicationContext)
    }

    /**
     * DNS-over-HTTPS via Cloudflare. Bypasses ISP DNS blocks (themoviedb.org / image.tmdb.org
     * are DNS-blocked by several Indian ISPs). Bootstrap hosts are IP literals, so resolving the
     * DoH endpoint itself needs no system DNS.
     */
    private fun dnsOverHttps(): Dns {
        val bootstrap = OkHttpClient.Builder().build()
        return DnsOverHttps.Builder()
            .client(bootstrap)
            .url("https://1.1.1.1/dns-query".toHttpUrl())
            .bootstrapDnsHosts(
                InetAddress.getByName("1.1.1.1"),
                InetAddress.getByName("1.0.0.1"),
            )
            .build()
    }
}
