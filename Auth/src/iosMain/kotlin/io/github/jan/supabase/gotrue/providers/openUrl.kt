package io.github.jan.supabase.gotrue.providers

import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.logging.d
import io.github.jan.supabase.logging.e
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

internal actual fun openUrl(url: NSURL) {
    UIApplication.sharedApplication.openURL(url, emptyMap<Any?, Any>()) {
        if(it) Auth.logger.d { "Successfully opened provider url in safari" } else Auth.logger.e { "Failed to open provider url in safari" }
    }
}