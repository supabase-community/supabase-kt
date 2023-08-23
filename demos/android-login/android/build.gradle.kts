plugins {
    id("org.jetbrains.compose")
    id("com.android.application")
    kotlin("android")
    kotlin("plugin.serialization")
}

group "io.github.jan.supabase"
version "1.0-SNAPSHOT"

repositories {
    maven { url = uri("https://jitpack.io") }
    mavenLocal()
}

dependencies {
    api(project(":common"))
    implementation(libs.androidx.activity.compose)
    implementation("com.github.stevdza-san:OneTapCompose:1.0.3")
    implementation("com.google.android.gms:play-services-auth:20.5.0")
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

compose {
    kotlinCompilerPlugin.set(dependencies.compiler.forKotlin("1.8.20"))
    kotlinCompilerPluginArgs.add("suppressKotlinVersionCompatibilityCheck=1.8.21")
}
