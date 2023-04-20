@file:OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)

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
        jvmToolchain(8)
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(compose.runtime)
                api(compose.foundation)
                api(compose.material3)
                api(compose.materialIconsExtended)
                api(libs.bundles.supabase)
                api(libs.koin.core)
                api(libs.filepicker)
            }
        }
        val nonJsMain by creating {
            dependencies {
                api(libs.ktor.cio)
            }
        }
        val androidMain by getting {
            dependsOn(nonJsMain)
            dependencies {
                api(libs.androidx.compat)
                api(libs.androidx.core)
                api(libs.koin.android)
                api(libs.androidx.lifecycle.viewmodel.ktx)
                api(libs.androidx.lifecycle.viewmodel.compose)
            }
        }
        val desktopMain by getting {
            dependsOn(nonJsMain)
            dependencies {
                api(compose.preview)
            }
        }
    }
}

android {
    compileSdk = 33
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    namespace = "io.github.jan.supabase.common"
    defaultConfig {
        minSdk = 26
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}
