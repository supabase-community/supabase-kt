plugins {
    kotlin("multiplatform")
    id("com.android.library")
}

group = "io.github.jan-tennert.supabase"
version = Versions.PROJECT
description = "Extends supabase-kt with a Storage Client"

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
    ios()
    iosSimulatorArm64()
    sourceSets {
        all {
            languageSettings.optIn("kotlin.RequiresOptIn")
        }
        val commonMain by getting {
            dependencies {
                api(project(":gotrue-kt"))
                implementation("io.github.reactivecircus.cache4k:cache4k:0.9.0")
            }
        }
        val nonJsMain by creating {
            dependsOn(commonMain)
        }
        val jvmMain by getting {
            dependsOn(nonJsMain)
            dependencies {
                api("io.tus.java.client:tus-java-client:0.5.0")
            }
        }
        val androidMain by getting {
            dependsOn(nonJsMain)
            dependencies {
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val jsMain by getting {
            dependencies {
            }
        }
        val iosMain by getting
        val iosSimulatorArm64Main by getting {
            dependsOn(iosMain)
        }
    }
}

android {
    compileSdk = 33
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    namespace = "io.github.jan.supabase.storage.library"
    defaultConfig {
        minSdk = 21
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}
