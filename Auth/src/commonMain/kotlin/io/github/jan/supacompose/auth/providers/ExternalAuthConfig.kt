package io.github.jan.supacompose.auth.providers

import com.soywiz.klock.TimeSpan
import com.soywiz.klock.seconds

data class ExternalAuthConfig(
    var httpPort: Int = 0,
    var timeout: TimeSpan = 60.seconds,
    var htmlText: String = "Logged in. You may continue in your app!",
    var htmlTitle: String = "SupaCompose",
    var htmlIconUrl: String = "https://supabase.com/brand-assets/supabase-logo-icon.png"
)