plugins {
    id(libs.plugins.kotlin.multiplatform.get().pluginId)
    id(libs.plugins.compose.plugin.get().pluginId)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlinx.plugin.serialization)
}

group = "io.supabase"
version = "1.0-SNAPSHOT"


kotlin {
    js(IR) {
        browser()
        binaries.executable()
    }
    sourceSets {
        val jsMain by getting {
            dependencies {
                implementation(project(":sample:chat-demo-mpp:common"))
            }
        }
    }
}

