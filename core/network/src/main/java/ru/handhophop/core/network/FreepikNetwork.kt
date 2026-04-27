package ru.handhophop.core.network

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import ru.handhophop.core.network.api.FreepikApiService
import java.util.concurrent.TimeUnit

object FreepikNetwork {
    private const val BASE_URL = "https://api.freepik.com/"
    private const val NETWORK_LOG_TAG = "FreepikNetwork"

    private var okHttpClient: OkHttpClient? = null
    private var retrofit: Retrofit? = null
    private var freepikApiInterceptor: FreepikApiInterceptor? = null
    private var service: FreepikApiService? = null

    fun getApiService(): FreepikApiService {
        if (service == null) {
            createInitialization()
        }
        return service!!
    }

    fun createInitialization() {
        freepikApiInterceptor = FreepikApiInterceptor()
        val loggingInterceptor = HttpLoggingInterceptor { message ->
            println("$NETWORK_LOG_TAG: $message")
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        okHttpClient = OkHttpClient.Builder()
            .addInterceptor(freepikApiInterceptor!!)
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

        service = retrofit!!.create(FreepikApiService::class.java)
    }
}
