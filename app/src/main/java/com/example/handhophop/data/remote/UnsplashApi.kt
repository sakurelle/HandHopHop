package com.example.handhophop.data.remote
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit

class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
            .newBuilder()
            .addHeader("Authorization", "Client-ID YOUR_ACCESS_KEY")
            .build()

        return chain.proceed(request)
    }
}

fun provideRetrofit(): Retrofit {
    val okHttp = OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor())
        .build()
    return Retrofit.Builder()
        .client(okHttp)
        .baseUrl("https://api.unsplash.com/")
        .build()
}