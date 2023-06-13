import java.net.URL

buildscript {
    dependencies {
        classpath(libs.kotlinx.atomicfu.plugin)
    }
}

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.dokka)
    alias(libs.plugins.kotlinx.plugin.serialization)
    alias(libs.plugins.maven.publish)
    alias(libs.plugins.detekt)
}

val modules = listOf("supabase-kt", "gotrue-kt", "postgrest-kt", "storage-kt", "realtime-kt", "functions-kt", "apollo-graphql")

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

configure(allprojects.filter { it.name in modules || it.name == "bom" }) {
    apply(plugin = "org.jetbrains.dokka")
    apply(plugin = "org.jetbrains.kotlin.plugin.serialization")
    apply(plugin = "kotlinx-atomicfu")
    apply(plugin = "com.vanniktech.maven.publish")

    mavenPublishing {
        publishToMavenCentral(com.vanniktech.maven.publish.SonatypeHost.S01)

        signAllPublications()

        coordinates("io.github.jan-tennert.supabase", this@configure.name, Versions.PROJECT)

        pom {
            name.set(this@configure.name)
            description.set(this@configure.description ?: "A Kotlin Multiplatform Supabase SDK")
            inceptionYear.set("2023")
            url.set("https://github.com/supabase-community/supabase-kt/")
            licenses {
                license {
                    name.set("MIT License")
                    url.set("https://mit-license.org/")
                    distribution.set("https://mit-license.org/")
                }
            }
            developers {
                developer {
                    id.set("TheRealJan")
                    name.set("Jan Tennert")
                    url.set("https://github.com/jan-tennert/")
                }
            }
            scm {
                url.set("https://github.com/supabase-community/supabase-kt/")
                connection.set("scm:git:git://github.com/supabase-community/supabase-kt.git")
                developerConnection.set("scm:git:ssh://git@github.com/supabase-community/supabase-kt.git")
            }
        }
    }
}

tasks.register("detektAll") {
    configure(allprojects.filter { it.name in modules }) {
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

configure(allprojects.filter { it.name in modules }) {
    apply(plugin = "io.gitlab.arturbosch.detekt")

    detekt {
        buildUponDefaultConfig = true
        config.setFrom(files("$rootDir/detekt.yml"))
        //baseline = file("$rootDir/config/detekt/baseline.xml")
    }

    tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
        jvmTarget = "1.8"
        reports {
            xml.required.set(true)
            html.required.set(true)
            txt.required.set(true)
            sarif.required.set(true)
            md.required.set(true)
        }
        basePath = rootDir.absolutePath
        //finalizedBy(detektReportMergeSarif)
    }
    /*detektReportMergeSarif {
        input.from(tasks.withType<Detekt>().map { it.sarifReportFile })
    }
    tasks.withType<DetektCreateBaselineTask>().configureEach {
        jvmTarget = "1.8"
    }*/
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
                localDirectory.set(projectDir.resolve("src"))
                remoteUrl.set(URL("https://github.com/supabase-community/supabase-kt/tree/master/$name/src"))
                remoteLineSuffix.set("#L")
            }
        }
    }
}

group = "io.github.jan-tennert.supabase"
version = Versions.PROJECT

@OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)
kotlin {
    targetHierarchy.default()
    jvm {
        jvmToolchain(8)
        compilations.all {
            kotlinOptions.freeCompilerArgs = listOf(
                "-Xjvm-default=all",  // use default methods in interfaces,
                "-Xlambdas=indy"      // use invokedynamic lambdas instead of synthetic classes
            )
        }
    }
    android {
        publishLibraryVariants("release", "debug")
    }
    js(IR) {
        browser {
            testTask {
                enabled = false
            }
        }
    }
    ios()
    iosSimulatorArm64()
    mingwX64()
    macosX64()
    macosArm64()
    sourceSets {
        all {
            languageSettings.optIn("kotlin.RequiresOptIn")
            languageSettings.optIn("io.github.jan.supabase.annotations.SupabaseInternal")
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
                api(libs.stately)
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

android {
    compileSdk = 33
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