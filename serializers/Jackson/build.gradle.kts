plugins {
    id(libs.plugins.kotlin.multiplatform.get().pluginId)
    id(libs.plugins.android.library.get().pluginId)
}

description = "Extends supabase-kt with a Jackson Serializer"

repositories {
    mavenCentral()
}

@OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)
kotlin {
    defaultConfig()
    jvm()
    androidTarget {
        publishLibraryVariants("release", "debug")
    }
    sourceSets {       val commonMain by getting {
            dependencies {
                implementation(project(":"))
                api(libs.bundles.jackson)
            }
        }
    }
}

configureAndroidTarget()