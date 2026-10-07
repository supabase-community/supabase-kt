package io.github.jan.supabase.auth.native

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.initDone

internal actual suspend fun Auth.setupNativePlatform() {
    initDone()
}