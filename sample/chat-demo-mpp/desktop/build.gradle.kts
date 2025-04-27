plugins {
    id(libs.plugins.kotlin.multiplatform.get().pluginId)
    id(libs.plugins.compose.plugin.get().pluginId)
    alias(libs.plugins.compose.compiler)
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
                implementation(project(":sample:chat-demo-mpp:common"))
                implementation(compose.desktop.currentOs)
            }
        }
        val jvmTest by getting
    }
}

compose.desktop.configureComposeDesktop("chat-demo-mpp")