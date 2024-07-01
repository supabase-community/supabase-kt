plugins {
   // alias(libs.plugins.complete.kotlin)
    id(libs.plugins.kotlin.multiplatform.get().pluginId)
    id(libs.plugins.android.library.get().pluginId)
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
                //withMingw() - not supported
            }
            group("nonDesktop") {
                //withAndroidTarget() android has its own implementation
                withIos()
                withTvos()
                withWatchos()
                withMingw()
                withJs()
            }
        }
    }
    allTargets()
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":"))
                implementation(libs.krypto)
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
                implementation(project(":test-common"))
            }
        }
        val androidMain by getting {
            dependencies {
                api(libs.androidx.startup.runtime)
                api(libs.androidx.browser)
            }
        }
    }
}

configureLibraryAndroidTarget()