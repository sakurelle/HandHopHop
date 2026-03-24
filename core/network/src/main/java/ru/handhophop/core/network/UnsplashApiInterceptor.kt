package ru.handhophop.core.network

import okhttp3.Interceptor
import okhttp3.Response

//object ApiKey {
//    val unsplashKey: String by lazy {
//        val props = Properties()
//        val userDir = System.getProperty("user.dir")
//        val propsFile = File(userDir, "local.properties")
//
//        if (propsFile.exists()) {
//            props.load(propsFile.inputStream())
//            props.getProperty("unsplash_access_key") ?: ""
//        } else {
//            println("ERROR: no key found")
//            ""
//        }
//    }
//}

private const val API_KEY = "Q0x4v-22LVLs7irtDdDDnu17FLl184JdamzeyBenrH0"

class UnsplashApiInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
            .newBuilder()
            .addHeader("Authorization", "Client-ID ${API_KEY}")
            .build()

        return chain.proceed(request)
    }
}