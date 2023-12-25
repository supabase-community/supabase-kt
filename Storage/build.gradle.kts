plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
}

description = "Extends supabase-kt with a Storage Client"

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
                api(project(":gotrue-kt"))
             //   api(libs.cache4k)
            }
        }
        val nonJsMain by creating {
            dependsOn(commonMain)
        }
        val nonLinuxMain by creating {
            dependsOn(commonMain)
            dependencies {
                api(libs.bundles.multiplatform.settings)
            }
        }
        val jvmMain by getting {
            dependsOn(nonJsMain)
            dependsOn(nonLinuxMain)
        }
        val androidMain by getting {
            dependsOn(nonJsMain)
            dependsOn(nonLinuxMain)
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.bundles.testing)
            }
        }
        val jsMain by getting {
            dependsOn(nonLinuxMain)
        }
        val appleMain by getting {
            dependsOn(nonLinuxMain)
        }
        val iosMain by getting {
            dependsOn(appleMain)
        }
        val mingwX64Main by getting {
            dependsOn(nonLinuxMain)
        }
        val iosSimulatorArm64Main by getting {
            dependsOn(iosMain)
        }
    }
}

android {
    compileSdk = 34
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
