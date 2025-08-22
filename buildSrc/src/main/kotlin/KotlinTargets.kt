import com.android.build.api.dsl.androidLibrary
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.extra
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
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

fun KotlinMultiplatformExtension.configuredJvmTarget(jvmTarget: JvmTarget = JvmTarget.JVM_1_8) {
    jvm {
        compilerOptions.jvmTarget = jvmTarget
    }
}

fun KotlinMultiplatformExtension.configuredAndroidTarget(
    jvmTarget: JvmTarget = JvmTarget.JVM_1_8,
) {
    androidLibrary {
        compileSdk = 35
        minSdk = 21
        this.namespace = namespace ?: "${project.extra["base-group"].toString().replace("-", ".")}.${project.name.replace("-", "")}.library"
        withHostTestBuilder {}.configure {}
        compilations.configureEach {
            compilerOptions.configure {
                this.jvmTarget.set(jvmTarget)
            }
        }
    }
}

@OptIn(ExperimentalKotlinGradlePluginApi::class)
fun KotlinMultiplatformExtension.jvmTargets(jvmTarget: JvmTarget = JvmTarget.JVM_1_8) {
    configuredAndroidTarget(jvmTarget)
    configuredJvmTarget(jvmTarget)
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

@OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
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

fun KotlinMultiplatformExtension.composeTargets(
    jvmTarget: JvmTarget = JvmTarget.JVM_1_8,
) {
    jvmTargets(jvmTarget)
    jsTarget()
    iosTargets()
    wasmJsTarget()
}