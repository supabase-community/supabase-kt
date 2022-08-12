plugins {
    id("org.jetbrains.compose") version Versions.COMPOSE
    id("com.android.application")
    kotlin("android")
}

group = "io.github.jan-tennert.supacompose.android"
version = Versions.SUPACOMPOSE

repositories {
    jcenter()
    mavenCentral()
}

dependencies {
    implementation(project(":"))
    implementation(project(":Supacompose-Auth"))
    implementation(project(":Supacompose-Storage"))
    implementation(compose.runtime)
    implementation(compose.ui)
    implementation(compose.foundation)
    implementation(compose.material)
    implementation("io.ktor:ktor-client-cio:${Versions.KTOR}")
    implementation("androidx.activity:activity-compose:${Versions.ACTIVITY}")
}

android {
    compileSdk = 31
    defaultConfig {
        applicationId = "io.github.jan.supacompose.android"
        minSdk = 24
        targetSdk = 31
        versionCode = 1
        versionName = Versions.SUPACOMPOSE
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
}