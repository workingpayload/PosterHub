package com.example.postershub

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.crossfade
import com.example.postershub.di.ServiceLocator

class PostersHubApp : Application(), SingletonImageLoader.Factory {

    override fun onCreate() {
        super.onCreate()
        ServiceLocator.init(this)
    }

    /** Route Coil through the same authenticated OkHttp client, with crossfade enabled. */
    override fun newImageLoader(context: PlatformContext): ImageLoader =
        ImageLoader.Builder(context)
            .components {
                add(OkHttpNetworkFetcherFactory(callFactory = { ServiceLocator.okHttpClient }))
            }
            .crossfade(true)
            .build()
}
