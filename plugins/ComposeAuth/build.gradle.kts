import com.android.build.gradle.internal.lint.AndroidLintAnalysisTask
import com.android.build.gradle.internal.lint.LintModelWriterTask
import com.android.build.gradle.internal.tasks.LintModelMetadataTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id(libs.plugins.kotlin.multiplatform.get().pluginId)
    id(libs.plugins.android.library.get().pluginId)
    id(libs.plugins.compose.plugin.get().pluginId)
    alias(libs.plugins.compose.compiler)
}

description = "Extends gotrue-kt with Native Auth composables"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

@OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)
kotlin {
    defaultConfig()
    applyDefaultHierarchyTemplate {
        common {
            group("noDefault") {
                withJvm()
                withJs()
                withWasmJs()
            }
        }
    }
    jvmToolchain(11)
    composeTargets(JvmTarget.JVM_11)
    sourceSets {
        val commonMain by getting {
            dependencies {
                addModules(SupabaseModule.AUTH)
                implementation(compose.runtime)
          //      implementation(libs.krypto)
            }
        }
        val androidMain by getting {
            dependencies {
                api(libs.android.google.id)
                api(libs.androidx.credentials)
                api(libs.androidx.credentials.play.services)
                implementation(libs.androidx.activity.compose)
            }
        }
    }
}

configureLibraryAndroidTarget(javaVersion = JavaVersion.VERSION_11)

//see https://github.com/JetBrains/compose-multiplatform/issues/4739
tasks.withType<LintModelWriterTask> {
    dependsOn("generateResourceAccessorsForAndroidUnitTest")
}
tasks.withType<LintModelMetadataTask> {
    dependsOn("generateResourceAccessorsForAndroidUnitTest")
}
tasks.withType<AndroidLintAnalysisTask> {
    dependsOn("generateResourceAccessorsForAndroidUnitTest")
}