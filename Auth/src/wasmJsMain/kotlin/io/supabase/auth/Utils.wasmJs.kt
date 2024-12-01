package io.supabase.auth

import io.supabase.SupabaseClient
import kotlinx.browser.window

internal actual suspend fun SupabaseClient.openExternalUrl(url: String) {
    window.location.href = url
}