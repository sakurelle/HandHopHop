package ru.handhophop.core.network

import okhttp3.Interceptor
import okhttp3.Response

private const val API_KEY = "FPSX5b49b9e0913948138b3ec783420d195d"

class FreepikApiInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
            .newBuilder()
            .addHeader("x-freepik-api-key", API_KEY)
            .build()

        return chain.proceed(request)
    }
}
