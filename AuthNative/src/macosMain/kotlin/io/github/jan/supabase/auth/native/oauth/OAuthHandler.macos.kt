package io.github.jan.supabase.auth.native.oauth

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.logging.d
import io.github.jan.supabase.logging.e
import platform.AppKit.NSWorkspace
import platform.AppKit.NSWorkspaceOpenConfiguration
import platform.Foundation.NSURL

internal actual suspend fun SupabaseClient.openExternalUrl(url: String) {
    NSWorkspace.sharedWorkspace().openURL(NSURL(string = url), NSWorkspaceOpenConfiguration()) { _, error ->
        if(error != null) logger.d { "Successfully opened provider url in safari" } else logger.e { "Failed to open provider url in safari" }
    }
}