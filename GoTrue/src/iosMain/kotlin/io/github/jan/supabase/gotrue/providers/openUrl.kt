package io.github.jan.supabase.gotrue.providers

import co.touchlab.kermit.Logger
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

internal actual fun openUrl(url: NSURL) {
    UIApplication.sharedApplication.openURL(url, emptyMap<Any?, Any>()) {
        if(it) Logger.d { "Successfully opened provider url in safari" } else Logger.e { "Failed to open provider url in safari" }
    }
}