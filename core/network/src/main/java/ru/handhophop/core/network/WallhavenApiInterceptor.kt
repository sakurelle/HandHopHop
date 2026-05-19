package ru.handhophop.core.network

import okhttp3.Interceptor
import okhttp3.Response

private const val API_KEY = "ZHm9y9Rpp1FUQhd5EX0UJqRslboeVdpZ"

class WallhavenApiInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val url = originalRequest.url

        val newUrl = if (API_KEY.isNotBlank() && API_KEY != "YOUR_API_KEY_HERE") {
            url.newBuilder()
                .addQueryParameter("apikey", API_KEY)
                .build()
        } else {
            url
        }

        val request = originalRequest.newBuilder()
            .url(newUrl)
            .build()

        return chain.proceed(request)
    }
}
