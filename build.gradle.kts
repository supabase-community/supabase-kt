import java.net.URL

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
    alias(libs.plugins.dokka)
    alias(libs.plugins.kotlinx.plugin.serialization)
    alias(libs.plugins.maven.publish)
    alias(libs.plugins.detekt)
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

    group = "io.github.jan-tennert.supabase"
    version = extra["supabase-version"].toString()

    mavenPublishing {
        publishToMavenCentral(com.vanniktech.maven.publish.SonatypeHost.S01)

        signAllPublications()
        coordinates("io.github.jan-tennert.supabase", this@submodules.name, extra["supabase-version"].toString())

        pom {
            name.set(this@submodules.name)
            description.set(this@submodules.description ?: "A Kotlin Multiplatform Supabase SDK")
            inceptionYear.set("2024")
            url.set("https://github.com/supabase-community/supabase-kt/")
            licenses {
                license {
                    name = "MIT License"
                    url = "https://github.com/supabase-community/supabase-kt/blob/master/LICENSE"
                    distribution = "https://github.com/supabase-community/supabase-kt/blob/master/LICENSE"
                }
            }
            developers {
                developer {
                    id = "TheRealJan"
                    name = "Jan Tennert"
                    url = "https://github.com/jan-tennert/"
                }
            }
            scm {
                url = "https://github.com/supabase-community/supabase-kt/"
                connection = "scm:git:git://github.com/supabase-community/supabase-kt.git"
                developerConnection = "scm:git:ssh://git@github.com/supabase-community/supabase-kt.git"
            }
        }
    }
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

    detekt {
        buildUponDefaultConfig = true
        config.setFrom(files("$rootDir/detekt.yml"))
        //baseline = file("$rootDir/config/detekt/baseline.xml")
    }

    tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
        jvmTarget = "1.8"
        reports {
            xml.required = true
            html.required = true
            txt.required = true
            sarif.required = true
            md.required = true
        }
        basePath = rootDir.absolutePath
    }
    tasks.withType<org.jetbrains.dokka.gradle.DokkaTaskPartial>().configureEach {
        dokkaSourceSets.configureEach {
            sourceLink {
                val name = when(moduleName.get()) {
                    "functions-kt" -> "Functions"
                    "gotrue-kt" -> "GoTrue"
                    "postgrest-kt" -> "Postgrest"
                    "realtime-kt" -> "Realtime"
                    "storage-kt" -> "Storage"
                    "apollo-graphql" -> "plugins/ApolloGraphQL"
                    else -> ""
                }
                localDirectory = projectDir.resolve("src")
                remoteUrl = URL("https://github.com/supabase-community/supabase-kt/tree/master/$name/src")
                remoteLineSuffix = "#L"
            }
        }
    }
}

@OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)
kotlin {
    applyDefaultHierarchyTemplate()
    jvmToolchain(8)
    jvm()
    androidTarget {
        publishLibraryVariants("release", "debug")
    }
    js(IR) {
        browser {
            testTask {
                enabled = false
            }
        }
        nodejs {
            testTask {
                enabled = false
            }
        }
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    mingwX64()
    macosX64()
    macosArm64()
    linuxX64()
    watchosArm64()
    watchosX64()
    watchosSimulatorArm64()
    tvosX64()
    tvosArm64()
    tvosSimulatorArm64()
    sourceSets {
        all {
            languageSettings.optIn("kotlin.RequiresOptIn")
            languageSettings.optIn("io.github.jan.supabase.annotations.SupabaseInternal")
            languageSettings.optIn("io.github.jan.supabase.annotations.SupabaseExperimental")
            compilerOptions.freeCompilerArgs.add("-Xexpect-actual-classes")
        }
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
        val appleMain by getting
        val macosMain by getting
    }
}

android {
    compileSdk = 34
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    namespace = "io.github.jan.supabase.library"
    defaultConfig {
        minSdk = 21
    }
    lint {
        abortOnError = false
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}
