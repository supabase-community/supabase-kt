plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("com.louiscad.complete-kotlin") version "1.1.0"
}

group = "io.github.jan-tennert.supabase"
version = Versions.SUPABASEKT
description = "Extends supabase-kt with a Auth Client"

repositories {
    mavenCentral()
}

kotlin {
    /** Targets configuration omitted.
     *  To find out how to configure the targets, please follow the link:
     *  https://kotlinlang.org/docs/reference/building-mpp-with-gradle.html#setting-up-targets */

    jvm() {
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
    js(IR) {
        browser {
            testTask {
                enabled = false
                /**useKarma {
                useFirefox()
                }*/
            }
        }
    }
    //ios()
    sourceSets {
        all {
            languageSettings.optIn("kotlin.RequiresOptIn")
        }
        val commonMain by getting {
            dependencies {
                api(project(":"))
                implementation("com.russhwolf:multiplatform-settings-no-arg:${Versions.SETTINGS}")
                //implementation("com.russhwolf:multiplatform-settings-serialization:${Versions.SETTINGS}") (no support for coroutines)
                implementation("com.russhwolf:multiplatform-settings-coroutines:${Versions.SETTINGS}")
                implementation("com.squareup.okio:okio:3.2.0")
                // https://mvnrepository.com/artifact/io.ktor/ktor-server-core
            }
        }
        val commonTest by getting {
            dependencies {
                implementation("io.ktor:ktor-client-mock:${Versions.KTOR}")
                implementation(kotlin("test"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${Versions.COROUTINES}")
                implementation("com.russhwolf:multiplatform-settings-test:${Versions.SETTINGS}")
            }
        }
        val jvmMain by getting {
            dependencies {
           //     api("io.ktor:ktor-server-core:${Versions.KTOR}")
                // https://mvnrepository.com/artifact/io.ktor/ktor-server-core
              //  api("io.ktor:ktor-server-cio:${Versions.KTOR}")
                implementation("io.javalin:javalin:5.3.2")
                //logback
                // https://mvnrepository.com/artifact/ch.qos.logback/logback-classic

            }
        }
        val androidMain by getting {
            dependencies {
                api("androidx.startup:startup-runtime:1.1.1")
            }
        }
        val jsMain by getting
        //val iosMain by getting
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
    lint {
        //isAbortOnError = false
    }
}
