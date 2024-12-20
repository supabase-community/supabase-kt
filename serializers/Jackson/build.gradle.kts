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
    configuredAndroidTarget()
    configuredJvmTarget()
    sourceSets {
        val commonMain by getting {
            dependencies {
                addModules(SupabaseModule.SUPABASE)
                api(libs.bundles.jackson)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.bundles.testing)
                implementation(project(":test-common"))
            }
        }
    }
}

configureLibraryAndroidTarget()