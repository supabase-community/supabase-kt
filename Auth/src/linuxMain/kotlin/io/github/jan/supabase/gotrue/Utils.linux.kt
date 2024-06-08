package io.github.jan.supabase.gotrue

import io.github.jan.supabase.SupabaseClient
import platform.posix.system

internal actual suspend fun SupabaseClient.openExternalUrl(url: String) {
    system("xdg-open $url")
}