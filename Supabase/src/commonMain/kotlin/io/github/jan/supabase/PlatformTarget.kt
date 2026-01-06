@file:Suppress("UndocumentedPublicProperty")
package io.github.jan.supabase

import io.github.jan.supabase.logging.e

/**
 * Represents a target platform
 */
enum class PlatformTarget {
    JVM, ANDROID, JS, WASM_JS, IOS, WINDOWS, MACOS, TVOS, WATCHOS, LINUX;
}

data class OSInformation(
    val name: String,
    val version: String?
) {

    companion object {
        val CURRENT by lazy {
            try {
                getOSInformation()
            } catch (e: Exception) {
                SupabaseClient.LOGGER.e(e) {
                    "Failed to get OS information, please report this issue"
                }
                null
            }
        }
    }

}

/**
 * The current target platform
 */
expect val CurrentPlatformTarget: PlatformTarget

/**
 * The current operating system information
 */
internal expect fun getOSInformation(): OSInformation?
