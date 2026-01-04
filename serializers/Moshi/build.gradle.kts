plugins {
    id(libs.plugins.kotlin.multiplatform.get().pluginId)
    id(libs.plugins.android.kotlin.multiplatform.library.get().pluginId)
}

description = "Extends supabase-kt with a Moshi Serializer"

repositories {
    mavenCentral()
}

@OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)
kotlin {
    defaultConfig()
    configuredAndroidTarget()
    configuredJvmTarget()
    sourceSets {
        commonMain {
            dependencies {
                addModules(SupabaseModule.SUPABASE)
                implementation(libs.kotlin.reflect)
                implementation(libs.bundles.moshi)
            }
        }
        commonTest {
            dependencies {
                implementation(libs.bundles.testing)
                implementation(project(":test-common"))
            }
        }
    }
}

