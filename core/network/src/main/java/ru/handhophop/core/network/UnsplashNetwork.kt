package ru.handhophop.core.network

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import ru.handhophop.core.network.api.UnsplashApiService
import java.util.concurrent.TimeUnit
import retrofit2.Retrofit

object UnsplashNetwork {
    private const val BASE_URL = "https://api.unsplash.com/"

    fun create(): UnsplashApiService {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(UnsplashApi())
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        val json = Json {
            ignoreUnknownKeys = true
        }

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

        return retrofit.create(UnsplashApiService::class.java)
    }
}