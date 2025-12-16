plugins {
    id(libs.plugins.kotlin.multiplatform.get().pluginId)
    id(libs.plugins.android.kotlin.multiplatform.library.get().pluginId)
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
        commonMain {
            dependencies {
                addModules(SupabaseModule.SUPABASE)
                implementation(libs.bundles.testing)
            }
        }
        commonTest {
            dependencies {
                implementation(libs.bundles.testing)
            }
        }
    }
}

