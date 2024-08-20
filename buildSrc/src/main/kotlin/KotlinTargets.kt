import org.gradle.kotlin.dsl.assign
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

fun KotlinMultiplatformExtension.iosTargets() {
    iosX64()
    iosArm64()
    iosSimulatorArm64()
}

fun KotlinMultiplatformExtension.watchosTargets() {
    watchosArm64()
    watchosX64()
    watchosSimulatorArm64()
}

fun KotlinMultiplatformExtension.tvosTargets() {
    tvosX64()
    tvosArm64()
    tvosSimulatorArm64()
}

fun KotlinMultiplatformExtension.macosTargets() {
    macosX64()
    macosArm64()
}

fun KotlinMultiplatformExtension.desktopTargets() {
    mingwX64()
    macosTargets()
    linuxX64()
}

fun KotlinMultiplatformExtension.configuredJvmTarget() {
    jvm {
        compilerOptions.jvmTarget = JvmTarget.JVM_1_8
    }
}

fun KotlinMultiplatformExtension.configuredAndroidTarget() {
    androidTarget {
        publishLibraryVariants("release", "debug")
        compilerOptions.jvmTarget = JvmTarget.JVM_1_8
    }
}

@OptIn(ExperimentalKotlinGradlePluginApi::class)
fun KotlinMultiplatformExtension.jvmTargets() {
    configuredAndroidTarget()
    configuredJvmTarget()
}

fun KotlinMultiplatformExtension.jsTarget() {
    js {
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
}

fun KotlinMultiplatformExtension.allTargets() {
    jvmTargets()
    jsTarget()
    iosTargets()
    watchosTargets()
    tvosTargets()
    desktopTargets()
}

fun KotlinMultiplatformExtension.composeTargets() {
    jvmTargets()
    jsTarget()
    iosTargets()
}