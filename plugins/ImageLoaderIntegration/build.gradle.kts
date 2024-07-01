plugins {
    id(libs.plugins.kotlin.multiplatform.get().pluginId)
    id(libs.plugins.android.library.get().pluginId)
}

description = "Extends supabase-kt with a Compose-ImageLoader integration for easy image loading"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

@OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)
kotlin {
    defaultConfig()
    composeTargets()
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":storage-kt"))
                api(libs.imageloader)
                implementation(libs.okio)
            }
        }
    }
}

configureLibraryAndroidTarget()