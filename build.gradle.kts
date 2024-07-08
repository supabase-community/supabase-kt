@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

val excludedModules = listOf("plugins", "serializers", "test-common")

fun libraryModules(withBom: Boolean = true, init: Project.() -> Unit) = configure(
    allprojects.filter { it.name !in excludedModules && !it.path.contains("sample") && if(withBom) true else it.name != "bom" },
    init
)

plugins {
    id(libs.plugins.kotlin.multiplatform.get().pluginId)
    id(libs.plugins.android.library.get().pluginId)
    id(libs.plugins.detekt.get().pluginId)
    id(libs.plugins.dokka.get().pluginId)
    alias(libs.plugins.kotlinx.plugin.serialization)
    id(libs.plugins.maven.publish.get().pluginId)
    alias(libs.plugins.kotlinx.atomicfu)
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

libraryModules {
    apply(plugin = "org.jetbrains.dokka")
    apply(plugin = "org.jetbrains.kotlin.plugin.serialization")
    apply(plugin = "org.jetbrains.kotlinx.atomicfu")
    apply(plugin = "com.vanniktech.maven.publish")

    group = extra["base-group"].toString()
    version = supabaseVersion

    applyPublishing()
}

tasks.register("detektAll") {
    libraryModules(false) {
        this@register.dependsOn(tasks.withType<io.gitlab.arturbosch.detekt.Detekt>())
    }
}

// Configure Gradle Task to build all sample submodules at once
configure(allprojects.filter { it.parent?.name == "sample" }) {
    val children = this.childProjects
    this.tasks.register("buildAll") {
        children.values.forEach { child ->
            this.dependsOn(child.tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>())
        }
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

libraryModules(false) {
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