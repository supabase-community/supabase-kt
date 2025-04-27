plugins {
    id(libs.plugins.compose.plugin.get().pluginId)
    alias(libs.plugins.compose.compiler)
    id(libs.plugins.kotlin.multiplatform.get().pluginId)
    alias(libs.plugins.kotlinx.plugin.serialization)
}

group = "io.github.jan.supabase"
version = "1.0-SNAPSHOT"


kotlin {
    jvmToolchain(8)
    jvm()
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(project(":sample:multi-factor-auth:common"))
                implementation(compose.desktop.currentOs)
            }
        }
        val jvmTest by getting
    }
}

compose.desktop.configureComposeDesktop("multi-factor-auth")