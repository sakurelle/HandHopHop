package ru.handhophop.core.network

import okhttp3.Interceptor
import okhttp3.Response

private const val API_KEY = "FPSXb560b0811e3f949c5f71d61addabf80b"

class FreepikApiInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
            .newBuilder()
            .addHeader("x-freepik-api-key", API_KEY)
            .build()

        return chain.proceed(request)
    }
}
