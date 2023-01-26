package io.github.jan.supabase.gotrue.providers

import kotlinx.browser.window

actual class ExternalAuthConfig {

    var redirectUrl: String = window.location.origin

}