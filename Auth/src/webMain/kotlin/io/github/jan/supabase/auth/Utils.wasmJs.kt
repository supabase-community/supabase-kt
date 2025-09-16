package io.github.jan.supabase.auth

import io.github.jan.supabase.SupabaseClient
import kotlinx.browser.window

internal actual suspend fun SupabaseClient.openExternalUrl(url: String) {
    window.location.href = url
}