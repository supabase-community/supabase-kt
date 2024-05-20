import com.android.build.gradle.internal.lint.AndroidLintAnalysisTask
import com.android.build.gradle.internal.lint.LintModelWriterTask
import com.android.build.gradle.internal.tasks.LintModelMetadataTask

plugins {
    id(libs.plugins.kotlin.multiplatform.get().pluginId)
    id(libs.plugins.android.library.get().pluginId)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
}

description = "Extends supabase-kt with a Apollo GraphQL Client"

repositories {
    mavenCentral()
}

@OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)
kotlin {
    defaultConfig()
    applyDefaultHierarchyTemplate {
        common {
            group("nonJvm") {
                withIos()
                withJs()
            }
        }
    }
    composeTargets()
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(compose.ui)
                api(project(":gotrue-kt"))
                implementation(compose.material3)
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(libs.androidsvg)
            }
        }
    }
}

configureAndroidTarget()

//see https://github.com/JetBrains/compose-multiplatform/issues/4739
tasks.withType<LintModelWriterTask> {
    dependsOn("generateResourceAccessorsForAndroidUnitTest")
}
tasks.withType<LintModelMetadataTask> {
    dependsOn("generateResourceAccessorsForAndroidUnitTest")
}
tasks.withType<AndroidLintAnalysisTask> {
    dependsOn("generateResourceAccessorsForAndroidUnitTest")
}
