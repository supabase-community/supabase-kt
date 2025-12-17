plugins {
    id(libs.plugins.compose.plugin.get().pluginId)
    alias(libs.plugins.compose.compiler)
    id("com.android.application")
    id(libs.plugins.kotlin.android.get().pluginId)
    alias(libs.plugins.kotlinx.plugin.serialization)
}

group = "io.github.jan.supabase"
version = "1.0-SNAPSHOT"

dependencies {
    implementation(project(":sample:file-upload:common"))
    implementation(libs.androidx.activity.compose)
    implementation(libs.accompanist.permissions)
}

android {
    configureApplicationAndroidTarget(JavaVersion.VERSION_11)
}