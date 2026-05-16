plugins {
    alias(libs.plugins.android.library) // Use Android Library plugin
    alias(libs.plugins.kotlin.android)   // Use Kotlin Android plugin
}

android {
    namespace = "ru.handhophop.core.session"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11
    }
}
dependencies{
    implementation(project(":core:network"))
    implementation(libs.kotlinx.datetime)
}