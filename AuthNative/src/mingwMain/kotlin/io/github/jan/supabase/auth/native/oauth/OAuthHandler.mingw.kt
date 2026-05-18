package io.github.jan.supabase.auth.native.oauth

import io.github.jan.supabase.SupabaseClient
import platform.windows.SW_SHOWNORMAL
import platform.windows.ShellExecuteW

internal actual suspend fun SupabaseClient.openExternalUrl(url: String) {
    ShellExecuteW(null, "open", url, null, null, SW_SHOWNORMAL);
}