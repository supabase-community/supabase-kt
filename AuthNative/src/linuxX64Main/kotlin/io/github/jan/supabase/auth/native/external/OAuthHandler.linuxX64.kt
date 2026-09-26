package io.github.jan.supabase.auth.native.external

import io.github.jan.supabase.SupabaseClient
import platform.posix.system

internal actual suspend fun SupabaseClient.openExternalUrl(url: String) {
    system("xdg-open $url")
}