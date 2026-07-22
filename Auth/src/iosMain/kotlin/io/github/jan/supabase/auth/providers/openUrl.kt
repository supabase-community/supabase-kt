package io.github.jan.supabase.auth.providers

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.logging.d
import io.github.jan.supabase.logging.e
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

internal actual fun Auth.openUrl(url: NSURL) {
    UIApplication.sharedApplication.openURL(url, emptyMap<Any?, Any>()) {
        if(it) logger.d { "Successfully opened provider url in safari" } else logger.e { "Failed to open provider url in safari" }
    }
}