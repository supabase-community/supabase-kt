plugins {
    kotlin("multiplatform")
    id("com.android.library")
    alias(libs.plugins.complete.kotlin)
}

group = "io.github.jan-tennert.supabase"
version = Versions.PROJECT
description = "Extends supabase-kt with a Auth Client"

repositories {
    mavenCentral()
}

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
    //ios()
    sourceSets {
        all {
            languageSettings.optIn("kotlin.RequiresOptIn")
        }
        val commonMain by getting {
            dependencies {
                api(project(":"))
                api(libs.bundles.multiplatform.settings)
                implementation(libs.okio)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.ktor.client.mock)
                implementation(kotlin("test"))
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.multiplatform.settings.test)
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(libs.javalin)
            }
        }
        val androidMain by getting {
            dependencies {
                api(libs.androidx.startup.runtime)
            }
        }
        val jsMain by getting
        //val iosMain by getting
    }
}

android {
    compileSdk = 33
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
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
