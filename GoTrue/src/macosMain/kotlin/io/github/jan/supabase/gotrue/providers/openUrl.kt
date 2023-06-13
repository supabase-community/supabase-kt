package io.github.jan.supabase.gotrue.providers

import platform.AppKit.NSWorkspace
import platform.Foundation.NSURL

internal actual fun openUrl(url: NSURL) {
    NSWorkspace.sharedWorkspace().openURL(url)
}