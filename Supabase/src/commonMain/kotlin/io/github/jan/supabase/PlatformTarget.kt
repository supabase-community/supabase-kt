@file:Suppress("UndocumentedPublicProperty")
package io.github.jan.supabase

/**
 * Represents a target platform
 */
enum class PlatformTarget {
    JVM, ANDROID, JS, WASM_JS, IOS, WINDOWS, MACOS, TVOS, WATCHOS, LINUX;
}

/**
 * The current target platform
 */
expect val CurrentPlatformTarget: PlatformTarget