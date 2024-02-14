plugins {
    alias(libs.plugins.complete.kotlin)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
}

description = "Extends supabase-kt with a Auth Client"

repositories {
    mavenCentral()
}

@OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)
kotlin {
    applyDefaultHierarchyTemplate()
    jvm {
        jvmToolchain(8)
        compilations.all {
            kotlinOptions.freeCompilerArgs = listOf(
                "-Xjvm-default=all",  // use default methods in interfaces,
                "-Xlambdas=indy"      // use invokedynamic lambdas instead of synthetic classes
            )
        }
    }
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
        }
        val commonMain by getting {
            dependencies {
                api(project(":"))
                implementation(libs.krypto)
         //       api(libs.cache4k)
            }
        }
        val desktopMain by creating {
            dependsOn(commonMain)
            dependencies {
                api(libs.ktor.server.core)
                api(libs.ktor.server.cio)
            }
        }
        val nonDesktopMain by creating {
            dependsOn(commonMain)
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
            dependsOn(desktopMain)
        }
        val androidMain by getting {
            dependsOn(nonLinuxMain)
            dependencies {
                api(libs.androidx.startup.runtime)
                api(libs.androidx.browser)
            }
        }
        val mingwMain by getting {
            dependsOn(nonLinuxMain)
            dependsOn(nonDesktopMain) //no ktor server engine supports the windows target
            //dependsOn(desktopMain)
        }
        val appleMain by getting {
            dependsOn(nonLinuxMain)
            dependsOn(desktopMain)
        }
        val jsMain by getting {
            dependsOn(nonLinuxMain)
            dependsOn(nonDesktopMain)
        }
        val linuxMain by getting {
            dependsOn(desktopMain)
        }
        val iosMain by getting {
            dependsOn(nonDesktopMain)
        }
        val tvosMain by getting {
            dependsOn(nonDesktopMain)
        }
        val watchosMain by getting {
            dependsOn(nonDesktopMain)
        }
        val macosMain by getting {
            dependsOn(desktopMain)
        }
    }
}

android {
    compileSdk = 34
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
