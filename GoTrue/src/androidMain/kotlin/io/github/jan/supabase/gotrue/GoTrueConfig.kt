package io.github.jan.supabase.gotrue

import io.github.jan.supabase.gotrue.ExternalAuthAction.CUSTOM_TABS
import io.github.jan.supabase.gotrue.ExternalAuthAction.EXTERNAL_BROWSER
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
    var defaultExternalAuthAction: ExternalAuthAction = ExternalAuthAction.EXTERNAL_BROWSER

}

/**
 * Represents the available actions for external auth such as OAuth and SSO.
 * @property EXTERNAL_BROWSER Open the OAuth/SSO flow in an external browser
 * @property CUSTOM_TABS Open the OAuth/SSO flow in a custom tab
 * @see [GoTrueConfig.defaultExternalAuthAction]
 */
enum class ExternalAuthAction {
    EXTERNAL_BROWSER, CUSTOM_TABS
}
