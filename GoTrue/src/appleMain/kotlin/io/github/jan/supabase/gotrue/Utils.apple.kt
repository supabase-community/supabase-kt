package io.github.jan.supabase.gotrue

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.providers.openUrl
import platform.Foundation.NSURL

internal actual suspend fun SupabaseClient.openExternalUrl(url: String) {
    openUrl(NSURL(url))
}