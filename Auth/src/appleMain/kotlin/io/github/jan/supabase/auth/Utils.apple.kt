package io.github.jan.supabase.auth

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.providers.openUrl
import platform.Foundation.NSURL

internal actual suspend fun SupabaseClient.openExternalUrl(url: String) {
    openUrl(NSURL(string = url))
}