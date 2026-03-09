package com.example.handhophop.data.remote

import okhttp3.OkHttpClient
import retrofit2.Retrofit

class Interceptor(): OkHttpInterceptor {
    ...

    // передача приватного ключа в хедер запроса

    ...
}

fun provideRetrofit(): Retrofit {
    val okHttp = OkHttpClient.Builder()
        .addInterceptor(...)

    return Retrofit.Builder()
        .client(okHttp)
        .build()
}