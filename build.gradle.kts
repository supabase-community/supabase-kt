@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

val excludedModules = listOf("plugins", "serializers", "test-common")

fun submodules(init: Project.() -> Unit) = configure(allprojects.filter { it.name !in excludedModules }, init)

buildscript {
    dependencies {
        classpath(libs.kotlinx.atomicfu.plugin)
    }
}

plugins {
    id(libs.plugins.kotlin.multiplatform.get().pluginId)
    id(libs.plugins.android.library.get().pluginId)
    id(libs.plugins.detekt.get().pluginId)
    id(libs.plugins.dokka.get().pluginId)
    alias(libs.plugins.kotlinx.plugin.serialization)
    id(libs.plugins.maven.publish.get().pluginId)
}

allprojects {
    repositories {
        google()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev/")
        mavenCentral()
        maven {
            url = uri("https://maven.pkg.jetbrains.space/public/p/ktor/eap")
            name = "ktor-eap"
        }
    }
}

submodules {
    apply(plugin = "org.jetbrains.dokka")
    apply(plugin = "org.jetbrains.kotlin.plugin.serialization")
    apply(plugin = "kotlinx-atomicfu")
    apply(plugin = "com.vanniktech.maven.publish")

    group = extra["base-group"].toString()
    version = extra["supabase-version"].toString()

    applyPublishing()
}

tasks.register("detektAll") {
    configure(allprojects.filter { it.name != "bom" && it.name !in excludedModules }) {
        this@register.dependsOn(tasks.withType<io.gitlab.arturbosch.detekt.Detekt>())
    }
}

val buildConfigGenerator by tasks.registering(Sync::class) {

    from(
        resources.text.fromString(
            """
        |package io.github.jan.supabase
        |
        |import io.github.jan.supabase.annotations.SupabaseInternal
        |
        |@SupabaseInternal
        |object BuildConfig {
        |  const val PROJECT_VERSION = "${project.version}"
        |}
        |
      """.trimMargin()
        )
    ) {
        rename { "BuildConfig.kt" } // set the file name
        into("io/github/jan/supabase/") // change the directory to match the package
    }

    into(layout.buildDirectory.dir("generated-src/kotlin/"))
}

configure(allprojects.filter { it.name != "bom" && it.name !in excludedModules }) {
    apply(plugin = "io.gitlab.arturbosch.detekt")

    applyDokkaWithConfiguration()
    applyDetektWithConfiguration()

}

@OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)
kotlin {
    defaultConfig()
    allTargets()
    sourceSets {
        val commonMain by getting {
            kotlin.srcDir(
                // convert the task to a file-provider
                buildConfigGenerator.map { it.destinationDir }
            )
            dependencies {
                api(libs.kotlinx.datetime)
                api(libs.kotlinx.coroutines.core)
                api(libs.kermit)
                api(libs.bundles.ktor.client)
                api(libs.kotlinx.atomicfu)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.bundles.testing)
            }
        }
        val androidMain by getting {
            dependencies {
                api(libs.android.lifecycle.process)
            }
        }
    }
}

configureLibraryAndroidTarget()