package io.github.jan.supabase.auth

import io.github.jan.supabase.auth.oauth.listenForDeeplinks

internal actual suspend fun Auth.setupNativePlatform() {
    listenForDeeplinks()
    addLifecycleCallbacks(this)
}