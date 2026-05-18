package io.github.jan.supabase.auth.native.native

import io.github.jan.supabase.auth.Auth
import io.ktor.util.PlatformUtils.IS_BROWSER
import kotlinx.browser.window

internal actual fun Auth.defaultPlatformRedirectUrl(): String? {
    return if(IS_BROWSER) window.location.origin else null
}