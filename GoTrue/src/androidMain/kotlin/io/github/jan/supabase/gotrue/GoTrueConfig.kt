package io.github.jan.supabase.gotrue

import io.github.jan.supabase.plugins.CustomSerializationConfig
import io.github.jan.supabase.plugins.MainConfig

/**
 * The configuration for [GoTrue]
 */
actual class GoTrueConfig : MainConfig, CustomSerializationConfig, GoTrueConfigDefaults() {

    /**
     * The scheme for the redirect url, when using deep linking
     */
    var scheme: String = "supabase"

    /**
     * The host for the redirect url, when using deep linking
     */
    var host: String = "login"

    /**
     * Whether to stop auto-refresh on focus loss, and resume it on focus again
     */
    var enableLifecycleCallbacks: Boolean = true

}