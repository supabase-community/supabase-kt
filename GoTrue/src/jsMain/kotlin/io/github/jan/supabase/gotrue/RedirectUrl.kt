package io.github.jan.supabase.gotrue

import kotlinx.browser.window

actual fun GoTrue.generateRedirectUrl(fallbackUrl: String?): String? {
    return window.location.origin
}