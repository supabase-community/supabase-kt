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
    linuxX64()
    linuxArm64()
    sourceSets {
        all {
            languageSettings.optIn("kotlin.RequiresOptIn")
            languageSettings.optIn("io.github.jan.supabase.annotations.SupabaseInternal")
        }
        val commonMain by getting {
            dependencies {
                api(project(":"))
                implementation(libs.krypto)
                api(libs.cache4k)
            }
        }
        val nonLinuxMain by creating {
            dependsOn(commonMain)
            dependencies {
                api(libs.bundles.multiplatform.settings)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.bundles.testing)
            }
        }
        val jvmMain by getting {
            dependsOn(nonLinuxMain)
            dependencies {
                implementation(libs.javalin)
            }
        }
        val androidMain by getting {
            dependsOn(nonLinuxMain)
            dependencies {
                api(libs.androidx.startup.runtime)
            }
        }
        val mingwX64Main by getting {
            dependsOn(nonLinuxMain)
        }
        val appleMain by getting {
            dependsOn(nonLinuxMain)
        }
        val jsMain by getting {
            dependsOn(nonLinuxMain)
        }
        val linuxMain by getting
    }
}

android {
    compileSdk = 33
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    namespace = "io.github.jan.supabase.gotrue.library"
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
