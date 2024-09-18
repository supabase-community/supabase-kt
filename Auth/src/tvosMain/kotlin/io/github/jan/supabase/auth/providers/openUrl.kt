package io.github.jan.supabase.auth.providers

import platform.Foundation.NSURL

internal actual fun openUrl(url: NSURL) {
    error("Can't open url on tvOS")
}