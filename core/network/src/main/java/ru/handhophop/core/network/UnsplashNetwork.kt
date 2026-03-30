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

    private var okHttpClient: OkHttpClient? = null
    private var retrofit: Retrofit? = null
    private var unsplashApiInterceptor: UnsplashApiInterceptor? = null
    private var service: UnsplashApiService? = null

    fun getApiService(): UnsplashApiService {
        if (service == null) {
            createInitialization()
        }
        return service!!
    }

    fun createInitialization() {
        unsplashApiInterceptor = UnsplashApiInterceptor()

        okHttpClient = OkHttpClient.Builder()
            .addInterceptor(unsplashApiInterceptor!!)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        val json = Json {
            ignoreUnknownKeys = true
        }

        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient!!)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

        service = retrofit!!.create(UnsplashApiService::class.java)
    }
}