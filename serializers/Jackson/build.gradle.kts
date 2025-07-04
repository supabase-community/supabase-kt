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
        commonMain {
            dependencies {
                addModules(SupabaseModule.SUPABASE)
                api(libs.bundles.jackson)
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

configureLibraryAndroidTarget()