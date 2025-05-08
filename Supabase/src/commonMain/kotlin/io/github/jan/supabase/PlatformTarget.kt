@file:Suppress("UndocumentedPublicProperty")
package io.github.jan.supabase

/**
 * Represents a target platform
 */
enum class PlatformTarget {
    JVM, ANDROID, JS, WASM_JS, IOS, WINDOWS, MACOS, TVOS, WATCHOS, LINUX;
}

internal data class OSInformation(
    val name: String,
    val version: String
)

/**
 * The current target platform
 */
expect val CurrentPlatformTarget: PlatformTarget

/**
 * The current operating system information
 */
internal expect val OSInformation: OSInformation
