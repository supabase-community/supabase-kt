plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
    google()
}

dependencies {
    implementation(libs.kotlin.multiplatform.gradle)
    implementation(libs.android.gradle.plugin)
}