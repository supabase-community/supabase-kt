@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.plugin.KotlinHierarchyBuilder

fun KotlinHierarchyBuilder.androidAndJvmGroup() {
    group("androidAndJvm") {
        withJvm()
        withAndroidTarget()
    }
}

fun KotlinHierarchyBuilder.settingsGroup() {
    group("settings") {
        withJvm()
        withAndroidTarget()
        withJs()
        withMingwX64()
        withApple()
        withWasmJs()
    }
}