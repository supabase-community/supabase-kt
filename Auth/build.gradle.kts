plugins {
    kotlin("multiplatform")
    id("com.android.library")
}

group = "io.github.jan-tennert.supacompose"
version = Versions.SUPACOMPOSE
description = "Extends Supabase with a Auth Client"

repositories {
    mavenCentral()
}

kotlin {
    /** Targets configuration omitted.
     *  To find out how to configure the targets, please follow the link:
     *  https://kotlinlang.org/docs/reference/building-mpp-with-gradle.html#setting-up-targets */

    jvm("desktop") {
        compilations.all {
            kotlinOptions.jvmTarget = "11"
            kotlinOptions.freeCompilerArgs = listOf(
                "-Xjvm-default=all",  // use default methods in interfaces,
                "-Xlambdas=indy"      // use invokedynamic lambdas instead of synthetic classes
            )
        }
    }
    android {
        publishLibraryVariants("release", "debug")
    }
    js("web", IR) {
        browser {
            testTask {
                enabled = false
                /**useKarma {
                useFirefox()
                }*/
            }
        }
    }
    sourceSets {
        all {
            languageSettings.optIn("kotlin.RequiresOptIn")
        }
        val commonMain by getting {
            dependencies {
                api(project(":"))
                // https://mvnrepository.com/artifact/io.ktor/ktor-server-core
            }
        }
        val commonTest by getting {
            dependencies {
                implementation("io.ktor:ktor-client-mock:${Versions.KTOR}")
                implementation(kotlin("test"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${Versions.COROUTINES}")
            }
        }
        val desktopMain by getting {
            dependencies {
                api("io.ktor:ktor-server-core:${Versions.KTOR}")
                // https://mvnrepository.com/artifact/io.ktor/ktor-server-core
                api("io.ktor:ktor-server-cio:${Versions.KTOR}")
                api("io.ktor:ktor-client-cio:2.1.1")
            }
        }
        val androidMain by getting
        val webMain by getting
    }
}

android {
    compileSdk = 31
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk = 21
        targetSdk = 31
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}
