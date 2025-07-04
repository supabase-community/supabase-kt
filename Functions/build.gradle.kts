plugins {
    id(libs.plugins.kotlin.multiplatform.get().pluginId)
    id(libs.plugins.android.library.get().pluginId)
}

description = "Extends supabase-kt with a Edge Functions Client"

repositories {
    mavenCentral()
}

kotlin {
    defaultConfig()
    allTargets()
    sourceSets {
        commonMain {
            dependencies {
                addModules(SupabaseModule.AUTH, SupabaseModule.SUPABASE)
            }
        }
        commonTest {
            dependencies {
                implementation(project(":test-common"))
                implementation(libs.bundles.testing)
            }
        }
    }
}

android {
    compileSdk = 35
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    namespace = "io.github.jan.supabase.functions.library"
    defaultConfig {
        minSdk = 21
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}
