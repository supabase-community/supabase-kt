package io.github.jan.supabase.auth

import androidx.browser.customtabs.CustomTabsIntent
import io.github.jan.supabase.auth.providers.ExternalAuthConfig

/**
 * The configuration for [Auth]
 */
actual class AuthConfig : AuthConfigDefaults() {

    /**
     * The action to use for the OAuth flow. Can be overriden per-request in the [ExternalAuthConfig]
     */
    var defaultExternalAuthAction: ExternalAuthAction = ExternalAuthAction.DEFAULT

}

/**
 * Represents the available actions for external auth such as OAuth and SSO.
 * @property EXTERNAL_BROWSER Open the OAuth/SSO flow in an external browser
 * @property CUSTOM_TABS Open the OAuth/SSO flow in a custom tab
 * @see [AuthConfig.defaultExternalAuthAction]
 */
sealed interface ExternalAuthAction {

    /**
     * Open the OAuth/SSO flow in an external browser
     */
    data object ExternalBrowser : ExternalAuthAction

    /**
     * Open the OAuth/SSO flow in a custom tab
     * @property intentBuilder The builder for the custom tabs intent
     */
    data class CustomTabs(val intentBuilder: CustomTabsIntent.Builder.() -> Unit = {}) : ExternalAuthAction

    companion object {

        /**
         * The default action to use for the OAuth flow
         */
        val DEFAULT: ExternalAuthAction = ExternalBrowser

    }

}
