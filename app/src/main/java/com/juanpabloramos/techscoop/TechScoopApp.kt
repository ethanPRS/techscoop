package com.juanpabloramos.techscoop

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import okhttp3.OkHttpClient
import com.google.firebase.FirebaseApp
import com.juanpabloramos.techscoop.data.local.AppDatabase


class TechScoopApp : Application(), ImageLoaderFactory {

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        AppDatabase.get(this)
    }

    override fun newImageLoader(): ImageLoader {
        val okHttp = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val req = chain.request().newBuilder()
                    .header(
                        "User-Agent",
                        "Mozilla/5.0 (Linux; Android 13; Mobile) AppleWebKit/537.36 " +
                                "(KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36 TechScoop/1.0"
                    )
                    .header("Accept", "image/avif,image/webp,image/apng,image/*,*/*;q=0.8")
                    .build()
                chain.proceed(req)
            }
            .build()

        return ImageLoader.Builder(this)
            .okHttpClient(okHttp)
            .crossfade(300)
            .respectCacheHeaders(false)
            .build()
    }
}

