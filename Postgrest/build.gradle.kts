plugins {
    id(libs.plugins.kotlin.multiplatform.get().pluginId)
    id(libs.plugins.android.library.get().pluginId)
}

description = "Extends supabase-kt with a Postgrest Client"

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
                api(project(":gotrue-kt"))
                api(libs.kotlin.reflect)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.bundles.testing)
            }
        }
        val jvmMain by getting
        val androidMain by getting
        val jsMain by getting
        val appleMain by getting
        val macosMain by getting
        val iosMain by getting
        val iosSimulatorArm64Main by getting
    }
}

configureAndroidTarget("postgrest")