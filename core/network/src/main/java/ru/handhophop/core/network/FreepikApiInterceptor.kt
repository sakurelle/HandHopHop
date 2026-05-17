package ru.handhophop.core.network

import okhttp3.Interceptor
import okhttp3.Response

private const val API_KEY = "FPSX9b3d7f526566427eae7466504a4c5ec8"

class FreepikApiInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
            .newBuilder()
            .addHeader("x-freepik-api-key", API_KEY)
            .build()

        return chain.proceed(request)
    }
}
