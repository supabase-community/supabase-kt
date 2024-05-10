import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl

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
   // mingwX64()
    macosTargets()
   // linuxX64()
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

fun KotlinMultiplatformExtension.wasmJsTarget() {
    wasmJs {
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

@OptIn(ExperimentalWasmDsl::class)
fun KotlinMultiplatformExtension.allTargets() {
    jvmTargets()
    jsTarget()
    iosTargets()
    watchosTargets()
    tvosTargets()
    desktopTargets()
    wasmJsTarget()
}

fun KotlinMultiplatformExtension.composeTargets() {
    jvmTargets()
    jsTarget()
    iosTargets()
    wasmJsTarget()
}