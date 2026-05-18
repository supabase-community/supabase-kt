package io.github.jan.supabase.auth.native

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.native.deeplinks.handleDeeplinks
import kotlinx.coroutines.launch

internal actual suspend fun Auth.setupNativePlatform() {
    listenForDeeplinks()
}

private fun Auth.listenForDeeplinks() {
    authScope.launch {
        AuthFlowManager.redirectFlow.collect {
            handleDeeplinks(it)
        }
    }
}