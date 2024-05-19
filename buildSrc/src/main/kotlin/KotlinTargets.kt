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

fun KotlinMultiplatformExtension.jvmTargets() {
    jvm()
    androidTarget {
        publishLibraryVariants("release", "debug")
    }
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