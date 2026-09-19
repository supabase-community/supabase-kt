import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation

/**
 * Enables Kotlin's built-in ABI validation for a published library module.
 *
 * Every module publishes a committed reference dump under `<module>/api/`. `checkKotlinAbi` compares
 * the current compilation against it and fails on any difference; `updateKotlinAbi` rewrites it after
 * an intentional change. The dump is the mechanical gate that makes an accidental binary break
 * impossible to merge without someone explicitly re-recording the surface.
 *
 * This matters more than usual here: the public API is dense with `inline` functions and
 * `@PublishedApi internal` declarations (`createSupabaseClient`, `PluginManager.getPlugin`,
 * `SupabaseHttpClient.postJson`, `AuthImpl`), all of which are inlined into consumer bytecode. For
 * those, changing a body that looks internal is a *binary* break for already-compiled consumers, and
 * nothing else in the build would catch it.
 *
 * `@SupabaseInternal` declarations are deliberately kept in the dump. They are public Kotlin
 * declarations gated only by an opt-in annotation, so they are part of the real ABI, and having them
 * visible in a reviewable file is what makes internal-surface leakage noticeable.
 */
@OptIn(ExperimentalAbiValidation::class)
fun Project.applyAbiValidation() {
    // The root build script configures library modules before their own build scripts have applied
    // the Kotlin Multiplatform plugin, so the extension does not exist yet. Defer until it does.
    pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
        extensions.configure(KotlinMultiplatformExtension::class) {
            abiValidation {
                // Targets that cannot be built on the current host (e.g. Apple targets on a Linux CI
                // runner) keep their previously recorded declarations instead of being silently
                // dropped from the dump. Without this, running `updateKotlinAbi` on Linux would
                // erase the Apple surface and the next macOS run would report it as a huge
                // "addition".
                keepLocallyUnsupportedTargets.set(true)
            }
        }
    }
}
