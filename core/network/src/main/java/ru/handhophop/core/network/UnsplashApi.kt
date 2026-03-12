package ru.handhophop.core.network

import okhttp3.Interceptor
import okhttp3.Response
import java.io.File
import java.util.Properties

object ApiKey {
    val unsplashKey: String by lazy {
        val props = Properties()
        val userDir = System.getProperty("user.dir")
        val propsFile = File(userDir, "local.properties")

        if (propsFile.exists()) {
            props.load(propsFile.inputStream())
            props.getProperty("unsplash_access_key") ?: ""
        } else {
            println("ERROR: no key found")
            ""
        }
    }
}

class UnsplashApi : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
            .newBuilder()
            .addHeader("Authorization", "Client-ID ${ApiKey.unsplashKey}")
            .build()

        return chain.proceed(request)
    }
}