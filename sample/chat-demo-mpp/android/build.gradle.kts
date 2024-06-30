plugins {
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    id("com.android.application")
    id(libs.plugins.kotlin.android.get().pluginId)
    alias(libs.plugins.kotlinx.plugin.serialization)
}

group "io.github.jan.supabase"
version "1.0-SNAPSHOT"

dependencies {
    implementation(project(":sample:chat-demo-mpp:common"))
    implementation(libs.androidx.activity.compose)
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
}
