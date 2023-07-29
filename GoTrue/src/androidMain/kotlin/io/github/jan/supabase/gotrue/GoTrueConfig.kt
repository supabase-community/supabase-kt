package io.github.jan.supabase.gotrue

import io.github.jan.supabase.gotrue.OAuthAction.CUSTOM_TABS
import io.github.jan.supabase.gotrue.OAuthAction.EXTERNAL_BROWSER
import io.github.jan.supabase.plugins.CustomSerializationConfig
import io.github.jan.supabase.plugins.MainConfig

/**
 * The configuration for [GoTrue]
 */
actual class GoTrueConfig : MainConfig, CustomSerializationConfig, GoTrueConfigDefaults() {

    /**
     * The scheme for the redirect url, when using deep linking
     */
    var scheme: String? = null

    /**
     * The host for the redirect url, when using deep linking
     */
    var host: String? = null

    /**
     * Whether to stop auto-refresh on focus loss, and resume it on focus again
     */
    var enableLifecycleCallbacks: Boolean = true

    /**
     * The action to use for the OAuth flow
     */
    var defaultOAuthAction: OAuthAction = OAuthAction.EXTERNAL_BROWSER

}

/**
 * Represents the available actions for OAuth.
 * @property EXTERNAL_BROWSER Open the OAuth flow in an external browser
 * @property CUSTOM_TABS Open the OAuth flow in a custom tab
 * @see [GoTrueConfig.defaultOAuthAction]
 */
enum class OAuthAction {
    EXTERNAL_BROWSER, CUSTOM_TABS
}
