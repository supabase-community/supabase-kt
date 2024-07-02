plugins {
    id(libs.plugins.compose.plugin.get().pluginId)
    alias(libs.plugins.compose.compiler)
    id("com.android.application")
    id(libs.plugins.kotlin.android.get().pluginId)
    alias(libs.plugins.kotlinx.plugin.serialization)
}

group "io.github.jan.supabase"
version "1.0-SNAPSHOT"

dependencies {
    implementation(project(":sample:file-upload:common"))
    implementation(libs.androidx.activity.compose)
    implementation("com.google.accompanist:accompanist-permissions:0.31.3-beta")
}

android {
    compileSdk = 34
    defaultConfig {
        applicationId = "io.github.jan.supabase.android"
        minSdk = 26
        versionCode = 1
        versionName = "1.0-SNAPSHOT"
    }
    namespace = "io.github.jan.supabase.android"
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
}
