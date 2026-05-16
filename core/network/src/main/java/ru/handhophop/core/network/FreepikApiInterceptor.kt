package ru.handhophop.core.network

import okhttp3.Interceptor
import okhttp3.Response

private const val API_KEY = "FPSX940c778f4dfa9a3ea8b15e3473ef373a"

class FreepikApiInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
            .newBuilder()
            .addHeader("x-freepik-api-key", API_KEY)
            .build()

        return chain.proceed(request)
    }
}
