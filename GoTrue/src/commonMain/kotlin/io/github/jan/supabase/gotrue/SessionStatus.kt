package io.github.jan.supabase.gotrue

import io.github.jan.supabase.gotrue.user.UserSession
import kotlin.jvm.JvmInline

/**
 * Represents the status of the current session in [Auth]
 */
sealed interface SessionStatus {

    /**
     * This status means that the user is not logged in
     */
    data object NotAuthenticated : SessionStatus

    /**
     * This status means that [Auth] is currently loading the session from storage
     */
    data object LoadingFromStorage : SessionStatus

    /**
     * This status means that [Auth] had an error while refreshing the session
     */
    data object NetworkError : SessionStatus

    /**
     * This status means that [Auth] holds a valid session
     * @param session The session
     */
    @JvmInline
    value class Authenticated(val session: UserSession) : SessionStatus
}
