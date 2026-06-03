package io.github.jan.supabase.auth.native.external

import io.github.jan.supabase.SupabaseClient

internal actual suspend fun SupabaseClient.openExternalUrl(url: String) {
    throw UnsupportedOperationException("Not supported on tvOS")
}