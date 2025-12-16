package io.github.jan.supabase.auth

import io.github.jan.supabase.SupabaseClient
import io.ktor.util.PlatformUtils.IS_BROWSER
import kotlinx.browser.window

internal actual suspend fun SupabaseClient.openExternalUrl(url: String) {
    if(IS_BROWSER) {
        window.location.href = url
    } else {
        //TODO: Unsupported for now
    }
}