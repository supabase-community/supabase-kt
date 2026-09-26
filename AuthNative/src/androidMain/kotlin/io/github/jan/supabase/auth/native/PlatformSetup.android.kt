package io.github.jan.supabase.auth.native

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.native.deeplinks.listenForDeeplinks

internal actual suspend fun Auth.setupNativePlatform() {
    listenForDeeplinks()
    addLifecycleCallbacks(this)
}