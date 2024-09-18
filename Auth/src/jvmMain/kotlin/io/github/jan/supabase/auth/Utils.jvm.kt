package io.github.jan.supabase.auth

import io.github.jan.supabase.SupabaseClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.Desktop
import java.net.URI

internal actual suspend fun SupabaseClient.openExternalUrl(url: String) {
    withContext(Dispatchers.IO) {
        Desktop.getDesktop()
            .browse(URI(url))
    }
}
