@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import com.android.build.api.dsl.KotlinMultiplatformAndroidCompilation
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.plugin.KotlinHierarchyBuilder

fun KotlinHierarchyBuilder.androidAndJvmGroup() {
    group("androidAndJvm") {
        withJvm()
        //see https://youtrack.jetbrains.com/issue/KT-80409
        withCompilations { it is KotlinMultiplatformAndroidCompilation }
    }
}

fun KotlinHierarchyBuilder.settingsGroup() {
    group("settings") {
        withJvm()
        // see https://youtrack.jetbrains.com/issue/KT-80409
        withCompilations { it is KotlinMultiplatformAndroidCompilation }
        withJs()
        withMingwX64()
        withApple()
        withWasmJs()
    }
}