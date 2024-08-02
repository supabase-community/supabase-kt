plugins {
    id(libs.plugins.kotlin.multiplatform.get().pluginId)
    id(libs.plugins.android.library.get().pluginId)
}

description = "Common test module"

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
                addModules(SupabaseModule.SUPABASE)
                implementation(libs.bundles.testing)
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
        val iosMain by getting
        val iosSimulatorArm64Main by getting
    }
}

configureLibraryAndroidTarget()