@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnLockMismatchReport
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnRootExtension

val excludedModules = listOf("plugins", "serializers", "test-common")

fun libraryModules(withBom: Boolean = true, init: Project.() -> Unit) = configure(
    allprojects.filter { it.name !in excludedModules && !it.path.contains("sample") && if(withBom) true else it.name != "bom" },
    init
)

plugins {
    id(libs.plugins.kotlin.multiplatform.get().pluginId) apply false
    id(libs.plugins.android.library.get().pluginId) apply false
    id(libs.plugins.detekt.get().pluginId) apply false
    id(libs.plugins.dokka.get().pluginId) apply false
    alias(libs.plugins.kotlinx.plugin.serialization) apply false
    id(libs.plugins.maven.publish.get().pluginId) apply false
    alias(libs.plugins.kotlinx.atomicfu) apply false
}

allprojects {
    repositories {
        google()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev/")
        mavenCentral()
        maven {
            url = uri("https://maven.pkg.jetbrains.space/public/p/ktor/eap")
            name = "ktor-eap"
        }
    }
}

libraryModules {
    apply(plugin = "org.jetbrains.dokka")
    apply(plugin = "org.jetbrains.kotlin.plugin.serialization")
    apply(plugin = "org.jetbrains.kotlinx.atomicfu")
    apply(plugin = "com.vanniktech.maven.publish")

    group = extra["base-group"].toString()
    version = supabaseVersion

    applyPublishing()
}

val reportMerge by tasks.registering(io.gitlab.arturbosch.detekt.report.ReportMergeTask::class) {
    output.set(rootProject.layout.buildDirectory.file("reports/detekt/merge.sarif"))
}

libraryModules(false) {
    applyDokkaWithConfiguration()
    applyDetektWithConfiguration(reportMerge)
}

tasks.register("detektAll") {
    libraryModules(false) {
        this@register.dependsOn(tasks.withType<io.gitlab.arturbosch.detekt.Detekt>())
    }
}

// Configure Gradle Task to build all sample submodules at once
configure(allprojects.filter { it.parent?.name == "sample" }) {
    val children = this.childProjects
    this.tasks.register("buildAll") {
        children.values.forEach { child ->
            this.dependsOn(child.tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask<*>>())
        }
    }
}

rootProject.plugins.withType(org.jetbrains.kotlin.gradle.targets.js.yarn.YarnPlugin::class.java) {
    rootProject.the<YarnRootExtension>().yarnLockMismatchReport =
        YarnLockMismatchReport.WARNING
    rootProject.the<YarnRootExtension>().reportNewYarnLock = false
    rootProject.the<YarnRootExtension>().yarnLockAutoReplace = true
}