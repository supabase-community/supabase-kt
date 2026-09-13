@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnLockMismatchReport
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnRootExtension

val excludedModules = listOf("plugins", "serializers", "test-common", "integration-test")

private val libraryFilter = { withFilter: Boolean ->
    allprojects.filter { it.name !in excludedModules && !it.path.contains("sample") && if(withFilter) true else it.name != "bom" && it.name != it.rootProject.name }
}

fun libraryModules(withBom: Boolean = true, init: Project.() -> Unit) = configure(
    libraryFilter(withBom),
    init
)

/**
 * Every published library module, the core `supabase-kt` module included.
 *
 * [libraryFilter] excludes the root project by comparing `it.name != it.rootProject.name`, but the
 * core module is *also* named `supabase-kt` (see the rename in settings.gradle.kts), so that name
 * comparison drops the core module as collateral. Comparing project identity instead is what makes
 * the core module reachable -- this is why `Supabase/build.gradle.kts` has to apply the detekt and
 * dokka plugins by hand while every other module gets them from the root.
 */
private val publishedLibraryModules = allprojects.filter {
    it != rootProject &&
            it.name !in excludedModules &&
            !it.path.contains("sample") &&
            it.name != "bom"
}

fun publishedLibraryModules(init: Project.() -> Unit) = configure(publishedLibraryModules, init)

plugins {
    id(libs.plugins.kotlin.multiplatform.get().pluginId) apply false
    id(libs.plugins.android.kotlin.multiplatform.library.get().pluginId) apply false
    id(libs.plugins.detekt.get().pluginId) apply false
    id(libs.plugins.dokka.get().pluginId)
    alias(libs.plugins.kotlinx.plugin.serialization) apply false
    id(libs.plugins.maven.publish.get().pluginId) apply false
    id(libs.plugins.power.assert.get().pluginId) apply false
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

dependencies {
    libraryFilter(false).forEach {
        dokka(project(it.path))
    }
}

libraryModules {
    apply(plugin = "org.jetbrains.dokka")
    apply(plugin = "org.jetbrains.kotlin.plugin.serialization")
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
    applyPowerAssertConfiguration()
    applyDetektWithConfiguration(reportMerge)
}

publishedLibraryModules {
    applyAbiValidation()
}

// Aggregate entry points so CI and contributors have one command each, mirroring `detektAll`.
tasks.register("apiCheck") {
    group = "verification"
    description = "Verifies the public ABI of every published module against its committed dump."
    publishedLibraryModules {
        // Task path, not a TaskProvider: the ABI tasks are registered lazily once each module
        // applies the KMP plugin, which happens after this block runs.
        this@register.dependsOn("${this.path}:checkKotlinAbi")
    }
}

tasks.register("apiDump") {
    group = "verification"
    description = "Re-records the committed public ABI dump of every published module."
    publishedLibraryModules {
        this@register.dependsOn("${this.path}:updateKotlinAbi")
    }
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