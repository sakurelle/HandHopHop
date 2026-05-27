import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library) // Use Android Library plugin
    alias(libs.plugins.kotlin.android)   // Use Kotlin Android plugin
}

android {
    namespace = "ru.handhophop.core.session"
    compileSdk = libs.versions.sessionCompileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.androidMinSdk.get().toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(libs.versions.jvmTarget.get())
        targetCompatibility = JavaVersion.toVersion(libs.versions.jvmTarget.get())
    }
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.fromTarget(libs.versions.jvmTarget.get())
    }
}
dependencies{
    implementation(project(":core:network"))
    implementation(libs.kotlinx.datetime)
}
