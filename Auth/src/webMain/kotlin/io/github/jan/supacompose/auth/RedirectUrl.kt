package io.github.jan.supacompose.auth

import kotlinx.browser.window

actual fun Auth.generateRedirectUrl(fallbackUrl: String?): String? {
    return window.location.origin
}