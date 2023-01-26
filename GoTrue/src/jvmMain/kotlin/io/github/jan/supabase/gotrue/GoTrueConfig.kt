package io.github.jan.supabase.gotrue

import io.github.jan.supabase.plugins.MainConfig
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

actual class GoTrueConfig : MainConfig, GoTrueConfigDefaults() {

    /**
     * The port the web server is running on, when logging in with OAuth. Defaults to 0 (random port).
     */
    var httpPort: Int = 0

    /**
     * The timeout for the web server, when logging in with OAuth. Defaults to 1 minutes.
     */
    var timeout: Duration = 1.minutes

    /**
     * The title of the redirect page, when logging in with OAuth. Defaults to "Supabase Auth".
     */
    var htmlTitle: String = "Supabase Auth"

    /**
     * The text of the redirect page, when logging in with OAuth. Defaults to "Logged in. You may continue in your app.".
     */
    var htmlText: String = "Logged in. You may continue in your app."

    /**
     * The icon of the redirect page, when logging in with OAuth. Defaults to "https://supabase.com/brand-assets/supabase-logo-icon.png".
     */
    var htmlIconUrl: String = "https://supabase.com/brand-assets/supabase-logo-icon.png"

}