package ru.handhophop.core.network

import okhttp3.Interceptor
import okhttp3.Response

private const val API_KEY = "FPSXa8eca1611e16f95373a90c86c9e2d73b"

class FreepikApiInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
            .newBuilder()
            .addHeader("x-freepik-api-key", API_KEY)
            .build()

        return chain.proceed(request)
    }
}
