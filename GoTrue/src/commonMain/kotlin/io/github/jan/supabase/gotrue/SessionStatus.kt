package io.github.jan.supabase.gotrue

import io.github.jan.supabase.gotrue.user.UserSession
import kotlin.jvm.JvmInline

/**
 * Represents the status of the current session in [GoTrue]
 */
sealed interface SessionStatus {

    /**
     * This status means that the user is not logged in
     */
    object NotAuthenticated : SessionStatus

    /**
     * This status means that [GoTrue] is currently loading the session from storage
     */
    object LoadingFromStorage : SessionStatus

    /**
     * This status means that [GoTrue] had an error while refreshing the session
     */
    object NetworkError : SessionStatus

    /**
     * This status means that [GoTrue] holds a valid session
     * @param session The session
     */
    @JvmInline
    value class Authenticated(val session: UserSession) : SessionStatus
}
