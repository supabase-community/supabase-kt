@file:OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
import org.jetbrains.compose.compose

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("com.android.library")
    kotlin("plugin.serialization")
}

group = "io.github.jan.supabase"
version = "1.0-SNAPSHOT"

kotlin {
    android()
    jvm("desktop") {
        compilations.all {
            kotlinOptions.jvmTarget = "11"
        }
    }
    js(IR) {
        browser()
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(compose.runtime)
                api(compose.foundation)
                api(compose.material3)
                api(compose.materialIconsExtended)
             //   implementation(platform("io.github.jan-tennert.supabase:bom:${Versions.SUPABASE}"))
                api("io.github.jan-tennert.supabase:gotrue-kt:${Versions.SUPABASE}")
                api("io.github.jan-tennert.supabase:realtime-kt:${Versions.SUPABASE}")
                api("io.github.jan-tennert.supabase:postgrest-kt:${Versions.SUPABASE}")
                api("io.insert-koin:koin-core:${Versions.KOIN}")
            }
        }
        val androidMain by getting {
            dependencies {
                api("androidx.appcompat:appcompat:1.6.0")
                api("androidx.core:core-ktx:1.9.0")
                api("io.insert-koin:koin-android:${Versions.KOIN}")
                api("androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.LIFECYCLE}")
                api("androidx.lifecycle:lifecycle-viewmodel-compose:${Versions.LIFECYCLE}")
                //android ktor client engine
                api("io.ktor:ktor-client-cio:${Versions.KTOR}")
            }
        }
        val desktopMain by getting {
            dependencies {
                api(compose.preview)
                api("io.ktor:ktor-client-cio:${Versions.KTOR}")
            }
        }
        val jsMain by getting {
            dependencies {
                api("io.ktor:ktor-client-js:${Versions.KTOR}")
            }
        }
    }
}

android {
    compileSdkVersion(33)
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdkVersion(24)
        targetSdkVersion(33)
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}