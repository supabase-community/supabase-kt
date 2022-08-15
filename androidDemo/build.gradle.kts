plugins {
    id("org.jetbrains.compose") version "1.2.0-alpha01-dev686"
    id("com.android.application") version "7.2.0"
    kotlin("android") version "1.6.21"
}

group = "io.github.jan-tennert.supacompose.android"
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
    val supacompose = "0.1.0"
    implementation("io.github.jan-tennert.supacompose:Supacompose-Postgrest:$supacompose")
    implementation("io.github.jan-tennert.supacompose:Supacompose-Realtime:$supacompose")
    implementation(compose.runtime)
    implementation(compose.ui)
    implementation(compose.foundation)
    implementation(compose.material)
    implementation("io.ktor:ktor-client-cio:2.0.3")
    implementation("androidx.activity:activity-compose:1.4.0")
}

android {
    compileSdk = 31
    defaultConfig {
        applicationId = "io.github.jan.supacompose.android"
        minSdk = 24
        targetSdk = 31
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