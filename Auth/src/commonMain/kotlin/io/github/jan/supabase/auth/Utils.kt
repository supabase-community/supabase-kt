package io.github.jan.supabase.auth

import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.logging.e
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

@SupabaseInternal fun Auth.initDone() {
    if(sessionStatus.value is SessionStatus.Initializing) {
        setSessionStatus(SessionStatus.NotAuthenticated())
    }
}

// TODO: Maybe make an actual method
@SupabaseInternal suspend fun Auth.tryToGetUser(accessToken: String) = try {
    getUser(accessToken)
} catch (e: Exception) {
    currentCoroutineContext().ensureActive()
    logger.e(e) { "Couldn't retrieve user using access token $accessToken.\nIf you use the project secret, ignore this message." }
    null
}