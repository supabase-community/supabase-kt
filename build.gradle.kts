plugins {
    kotlin("multiplatform") version Versions.KOTLIN
    id("com.android.library")
    id("maven-publish")
    signing
    id("org.jetbrains.dokka") version Versions.DOKKA
    id("io.codearte.nexus-staging") version Versions.NEXUS_STAGING
    kotlin("plugin.serialization") version Versions.KOTLIN
    id("org.jetbrains.compose") version "1.1.0"
}

subprojects {
    apply(plugin = "signing")
    apply(plugin = "maven-publish")
    apply(plugin = "org.jetbrains.dokka")
    apply(plugin = "org.jetbrains.kotlin.plugin.serialization")
    //apply(plugin = "com.android.library")
    apply(plugin = "org.jetbrains.compose")
}

group = "io.github.jan-tennert.supacompose"
version = Versions.SUPABASE

kotlin {
    jvm("desktop") {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
            kotlinOptions.freeCompilerArgs = listOf(
                "-Xjvm-default=all",  // use default methods in interfaces,
                "-Xlambdas=indy"      // use invokedynamic lambdas instead of synthetic classes
            )
        }
    }
    android()
    sourceSets {
        all {
            languageSettings.optIn("kotlin.RequiresOptIn")
        }
        val commonMain by getting {
            dependencies {
                api("io.ktor:ktor-client-core:${Versions.KTOR}")
                api("io.ktor:ktor-client-content-negotiation:${Versions.KTOR}")
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.COROUTINES}")
                api("io.ktor:ktor-serialization-kotlinx-json:${Versions.KTOR}")
               // api("org.jetbrains.kotlinx:kotlinx-serialization-json:${Versions.SERIALIZATION}")
                //install klock
                api(compose.runtime)
                api(compose.foundation)
                api(compose.material)
                api("com.soywiz.korlibs.korio:korio:${Versions.KORLIBS}")
            }
        }
        val commonTest by getting
        val desktopMain by getting
        val desktopTest by getting
        val androidMain by getting {
            dependencies {
                api("com.google.android.material:material:1.6.0")
                api("androidx.core:core-ktx:${Versions.ANDROID_CORE}")
                api("androidx.activity:activity-compose:${Versions.ACTIVITY}")
                api("androidx.appcompat:appcompat:${Versions.ANDROID_COMPAT}")
            }
        }
        val androidTest by getting {
            dependencies {
                implementation("junit:junit:4.13")
            }
        }
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

android {
    compileSdk = 31
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk = 21
        targetSdk = 31
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}