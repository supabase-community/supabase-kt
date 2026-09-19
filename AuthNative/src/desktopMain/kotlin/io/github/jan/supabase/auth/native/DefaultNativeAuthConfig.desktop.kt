package io.github.jan.supabase.auth.native

import io.github.jan.supabase.auth.AuthConfig
import io.github.jan.supabase.auth.native.external.server.HttpCallbackHtml
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

actual class PlatformNativeAuthConfig actual constructor() : DefaultNativeAuthConfig()  {

    internal var httpCallbackConfig: HttpCallbackConfig = HttpCallbackConfig()

    /**
     * Configures the http callback for the web server, when logging in with OAuth or SSO.
     */
    fun AuthConfig.httpCallbackConfig(block: HttpCallbackConfig.() -> Unit) {
        platformConfig().httpCallbackConfig = HttpCallbackConfig().apply(block)
    }

}

/**
 * Http callback configuration for the web server, when logging in with OAuth.
 *
 * @property httpPort The port the web server is running on, when logging in with OAuth. Defaults to 0 (random port).
 * @property timeout The timeout for the web server, when logging in with OAuth. Defaults to 1 minute.
 * @property htmlTitle The title of the html page, when logging in with OAuth. Defaults to "Supabase Auth".
 * @property redirectHtml The html content of the redirect page, when logging in with OAuth. Defaults to a page with a title, text and icon.
 */
data class HttpCallbackConfig(
    var httpPort: Int = 0,
    var timeout: Duration = 1.minutes,
    var htmlTitle: String = "Supabase Auth",
    var redirectHtml: String = HttpCallbackHtml.redirectPage("https://supabase.com/brand-assets/supabase-logo-icon.png", "Supabase Auth", "Logged in. You may continue in your app")
)

