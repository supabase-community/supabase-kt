plugins {
    id(libs.plugins.kotlin.multiplatform.get().pluginId)
    id(libs.plugins.android.library.get().pluginId)
}

description = "Extends supabase-kt with a Storage Client"

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
                api(project(":gotrue-kt"))
             //   api(libs.cache4k)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(project(":test-common"))
                implementation(libs.bundles.testing)
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
        val jsMain by getting {
            dependsOn(nonLinuxMain)
        }
        val appleMain by getting {
            dependsOn(nonLinuxMain)
        }
        val iosMain by getting
        val mingwX64Main by getting {
            dependsOn(nonLinuxMain)
        }
        val iosSimulatorArm64Main by getting
    }
}

configureAndroidTarget()