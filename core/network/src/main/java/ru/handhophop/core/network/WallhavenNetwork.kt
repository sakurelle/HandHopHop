package ru.handhophop.core.network

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import ru.handhophop.core.network.api.WallhavenApiService
import java.util.concurrent.TimeUnit

object WallhavenNetwork {
    private const val BASE_URL = "https://wallhaven.cc/api/"
    private const val NETWORK_LOG_TAG = "WallhavenNetwork"

    private var okHttpClient: OkHttpClient? = null
    private var retrofit: Retrofit? = null
    private var wallhavenApiInterceptor: WallhavenApiInterceptor? = null
    private var service: WallhavenApiService? = null

    fun getApiService(): WallhavenApiService {
        if (service == null) {
            createInitialization()
        }
        return service!!
    }

    fun createInitialization() {
        wallhavenApiInterceptor = WallhavenApiInterceptor()
        val loggingInterceptor = HttpLoggingInterceptor { message ->
            println("$NETWORK_LOG_TAG: $message")
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        okHttpClient = OkHttpClient.Builder()
            .addInterceptor(wallhavenApiInterceptor!!)
            .addInterceptor(loggingInterceptor)
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

        service = retrofit!!.create(WallhavenApiService::class.java)
    }
}