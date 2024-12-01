package io.supabase.auth

import io.supabase.SupabaseClient
import io.supabase.auth.providers.openUrl
import platform.Foundation.NSURL

internal actual suspend fun SupabaseClient.openExternalUrl(url: String) {
    openUrl(NSURL(string = url))
}