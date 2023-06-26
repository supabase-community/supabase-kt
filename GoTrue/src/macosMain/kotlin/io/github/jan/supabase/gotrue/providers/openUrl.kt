package io.github.jan.supabase.gotrue.providers

import co.touchlab.kermit.Logger
import platform.AppKit.NSWorkspace
import platform.AppKit.NSWorkspaceOpenConfiguration
import platform.Foundation.NSURL

internal actual fun openUrl(url: NSURL) {
    NSWorkspace.sharedWorkspace().openURL(url, NSWorkspaceOpenConfiguration()) { _, error ->
        if(error != null) Logger.d { "Successfully opened provider url in safari" } else Logger.e { "Failed to open provider url in safari" }
    }
}