plugins {
   // alias(libs.plugins.complete.kotlin)
    id(libs.plugins.kotlin.multiplatform.get().pluginId)
    id(libs.plugins.android.library.get().pluginId)
}

description = "Extends supabase-kt with a Auth Client"

repositories {
    mavenCentral()
}

@OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)
kotlin {
    defaultConfig()
    allTargets()
    sourceSets {
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
                implementation(project(":test-common"))
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

configureAndroidTarget()