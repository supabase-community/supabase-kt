plugins {
    id(libs.plugins.kotlin.multiplatform.get().pluginId)
    id(libs.plugins.android.library.get().pluginId)
}

description = "Extends supabase-kt with a Realtime Client"

repositories {
    mavenCentral()
}

@OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)
kotlin {
    defaultConfig()
    allTargets()
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":${Constants.AUTH_ARTIFACT_ID}"))
                api(project(":postgrest-kt"))
                api(libs.ktor.client.websockets)
            }
        }
    }
}

configureAndroidTarget()