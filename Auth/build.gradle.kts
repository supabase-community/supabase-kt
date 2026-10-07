plugins {
   // alias(libs.plugins.complete.kotlin)
    id(libs.plugins.kotlin.multiplatform.get().pluginId)
    id(libs.plugins.android.kotlin.multiplatform.library.get().pluginId)
    id("kotlin-parcelize")
}

description = "Extends supabase-kt with a Auth Client"

repositories {
    mavenCentral()
}

@OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)
kotlin {
    defaultConfig()
    applyDefaultHierarchyTemplate {
        common {
            settingsGroup()
        }
    }
    allTargets()
    sourceSets {
        val commonMain by getting {
            dependencies {
                addModules(SupabaseModule.SUPABASE)
             //   implementation(libs.krypto)
             //   implementation(libs.secure.random) replaced by crypto
             //   api(libs.bundles.jwt)
                api(libs.okio)
                implementation(libs.crypto)
                implementation(libs.crypto.optimal)
            }
        }
        val settingsMain by getting {
            dependencies {
                api(libs.bundles.multiplatform.settings)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.bundles.testing)
                implementation(libs.turbine)
                implementation(project(":test-common"))
            }
        }
        val androidMain by getting
        val webMain by getting {
            dependencies {
                api(libs.kotlinx.browser)
            }
        }
    }
}

