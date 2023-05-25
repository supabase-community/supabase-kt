package io.github.jan.supabase.gotrue.providers

import kotlinx.browser.window

/**
 * Configuration for external authentication providers like Google, Twitter, etc.
 */
actual class ExternalAuthConfig: ExternalAuthConfigDefaults() {

    /**
     * The URL to redirect to after a successful login.
     *
     * Defaults to `Window.location.origin`
     */
    var redirectUrl: String = window.location.origin

}