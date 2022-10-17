plugins {
    kotlin("multiplatform")
    id("com.android.library")
}

group = "io.github.jan-tennert.supabase"
version = Versions.SUPABASEKT
description = "Extends Supacompose with a Realtime Client"

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
                api(project(":gotrue-kt"))
                api("io.ktor:ktor-client-websockets:${Versions.KTOR}")
            }
        }
        val desktopMain by getting  {
            /*dependencies {
                implementation("ch.qos.logback:logback-classic:1.3.0-beta0")
                api("io.ktor:ktor-client-cio:${Versions.KTOR}")
            }*/
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
