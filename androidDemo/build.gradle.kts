plugins {
    id("org.jetbrains.compose") version Versions.COMPOSE
    id("com.android.application")
    kotlin("android")
}

group = "io.github.jan-tennert.supabase.android"
version = "1.0"

repositories {
    google()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev/")
    mavenCentral()
    maven {
        url = uri("https://maven.pkg.jetbrains.space/public/p/ktor/eap")
        name = "ktor-eap"
    }
}
dependencies {
    api(project(":gotrue-kt"))
    //api(project(":Supacompose"))
    implementation(compose.runtime)
    implementation(compose.ui)
    implementation(compose.foundation)
    implementation(compose.material)
    implementation("io.ktor:ktor-client-cio:2.1.2")
    implementation("androidx.activity:activity-compose:1.6.0")
}

android {
    compileSdk = 33
    defaultConfig {
        applicationId = "io.github.jan.supabase.android"
        minSdk = 24
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"
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