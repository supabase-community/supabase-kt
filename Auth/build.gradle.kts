plugins {
   // alias(libs.plugins.complete.kotlin)
    id(libs.plugins.kotlin.multiplatform.get().pluginId)
    id(libs.plugins.android.kotlin.multiplatform.library.get().pluginId)
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
            group("desktop") {
                withJvm()
                withMacos()
                withLinux()
                withMingw()
            }
            group("web") {
                withJs()
                withWasmJs()
            }
            group("nonDesktop") {
                //withAndroidTarget() android has its own implementation
                withIos()
                withTvos()
                withWatchos()
                //withMingw()
                withJs()
                withWasmJs()
            }
        }
    }
    allTargets()
    sourceSets {
        val commonMain by getting {
            dependencies {
                addModules(SupabaseModule.SUPABASE)
             //   implementation(libs.krypto)
                implementation(libs.secure.random)
                api(libs.okio)
            }
        }
        val desktopMain by getting {
            dependencies {
                api(libs.ktor.server.core)
                api(libs.ktor.server.cio)
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
        val androidMain by getting {
            dependencies {
                api(libs.androidx.startup.runtime)
                api(libs.androidx.browser)
            }
        }
        val wasmJsMain by getting {
            dependencies {
                api(libs.kotlinx.browser)
            }
        }
    }
}

