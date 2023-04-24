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

configure(allprojects.filter { it.name in modules }) {
    tasks.withType<org.jetbrains.dokka.gradle.DokkaTaskPartial>().configureEach {
        dokkaSourceSets.configureEach {
            sourceLink {
                val name = when(moduleName.get()) {
                    "functions-kt" -> "Functions"
                    "gotrue-kt" -> "GoTrue"
                    "postgrest-kt" -> "Postgrest"
                    "realtime-kt" -> "Realtime"
                    "storage-kt" -> "Storage"
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

kotlin {
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
    sourceSets {
        all {
            languageSettings.optIn("kotlin.RequiresOptIn")
        }
        val commonMain by getting {
            dependencies {
                api(libs.kotlinx.datetime)
                api(libs.kotlinx.coroutines.core)
                api(libs.napier)
                api(libs.bundles.ktor.client)
                api(libs.kotlinx.atomicfu)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.ktor.client.mock)
                implementation(libs.kotlinx.coroutines.test)
            }
        }
        val jvmMain by getting {
        }
        val jvmTest by getting
        val androidMain by getting {
            dependencies {
                api(libs.android.lifecycle.process)
            }
        }
        val androidUnitTest by getting {
            dependencies {
                implementation("junit:junit:4.13.2")
            }
        }
        val jsMain by getting {
            dependencies {
              //  api(compose.web.core)
            }
        }
        val iosTest by getting
        val iosMain by getting
        val iosSimulatorArm64Main by getting {
            dependsOn(iosMain)
        }
        val iosSimulatorArm64Test by getting {
            dependsOn(iosTest)
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
