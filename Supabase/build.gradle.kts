plugins {
    id(libs.plugins.kotlin.multiplatform.get().pluginId)
   // id(libs.plugins.android.kotlin.multiplatform.library.get().pluginId)
    id(libs.plugins.android.kotlin.multiplatform.library.get().pluginId)
    id(libs.plugins.detekt.get().pluginId)
    id(libs.plugins.dokka.get().pluginId)
    alias(libs.plugins.kotlinx.plugin.serialization)
    id(libs.plugins.maven.publish.get().pluginId)
    alias(libs.plugins.kotlinx.atomicfu)
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

@OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)
kotlin {
    defaultConfig()
    allTargets()
    sourceSets {
        commonMain {
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
                api(libs.kotlinx.serialization.json)
            }
        }
        commonTest {
            dependencies {
                implementation(libs.bundles.testing)
            }
        }
        androidMain {
            dependencies {
                api(libs.android.lifecycle.process)
            }
        }
    }
}

