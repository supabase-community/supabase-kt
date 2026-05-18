plugins {
   // alias(libs.plugins.complete.kotlin)
    id(libs.plugins.kotlin.multiplatform.get().pluginId)
    id(libs.plugins.android.kotlin.multiplatform.library.get().pluginId)
    id("kotlin-parcelize")
}

description = "Extends supabase-kt with a Native Auth, OAuth and Passkey functionality"

repositories {
    mavenCentral()
}

@OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)
kotlin {
    defaultConfig()
    applyDefaultHierarchyTemplate {
        common {
            group("desktop") {
                withJvm()
                withMacos()
                withLinux()
                withMingw()
            }
            group("nonDesktop") {
                //withAndroidTarget() android has its own implementation
                withTvos()
                withWatchos()
                //withMingw()
                withJs()
                withWasmJs()
            }
            group("noRedirect") {
                withTvos()
                withWatchos()
                withMacos()
                withLinux()
                withMingw()
                withJvm()
            }
        }
    }
    allTargets()
    sourceSets {
        val commonMain by getting {
            dependencies {
                addModules(SupabaseModule.AUTH)
            }
        }
        val desktopMain by getting {
            dependencies {
                api(libs.ktor.server.core)
                api(libs.ktor.server.cio)
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
        val webMain by getting {
            dependencies {
                api(libs.kotlinx.browser)
            }
        }
    }
}

