package io.github.jan.supabase.auth

import io.github.jan.supabase.annotations.SupabaseInternal
import kotlinx.coroutines.launch

@SupabaseInternal
actual suspend fun Auth.setupPlatform() {
    listenForDeeplinks()
    initDone()
}

private fun Auth.listenForDeeplinks() {
    authScope.launch {
        AuthFlowManager.redirectFlow.collect {
            handleDeeplinks(it)
        }
    }
}