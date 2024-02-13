package io.github.jan.supabase.gotrue

import io.github.jan.supabase.gotrue.ExternalAuthAction.CUSTOM_TABS
import io.github.jan.supabase.gotrue.ExternalAuthAction.EXTERNAL_BROWSER

/**
 * Represents the available actions for external auth such as OAuth and SSO.
 * @property EXTERNAL_BROWSER Open the OAuth/SSO flow in an external browser
 * @property CUSTOM_TABS Open the OAuth/SSO flow in a custom tab
 * @see [AuthConfig.defaultExternalAuthAction]
 */
enum class ExternalAuthAction {
    EXTERNAL_BROWSER, CUSTOM_TABS
}
