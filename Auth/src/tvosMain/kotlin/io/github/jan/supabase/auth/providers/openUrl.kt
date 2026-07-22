package io.github.jan.supabase.auth.providers

import io.github.jan.supabase.auth.Auth
import platform.Foundation.NSURL

internal actual fun Auth.openUrl(url: NSURL) {
    error("Can't open url on tvOS")
}