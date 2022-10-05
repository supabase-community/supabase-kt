plugins {
    kotlin("multiplatform")
    id("com.android.library")
}

group = "io.github.jan-tennert.supacompose"
version = Versions.SUPACOMPOSE
description = "Extends Supabase with a Edge Functions Client"

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
                api(project(":Supacompose-Auth"))
                // https://mvnrepository.com/artifact/io.ktor/ktor-server-core
            }
        }
        val commonTest by getting
        val desktopMain by getting {
            dependencies {
                // add cio ktor client
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
