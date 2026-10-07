package io.github.jan.supabase.auth.status

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.user.UserSession

/**
 * Represents the status of the current session in [Auth]
 */
sealed interface SessionStatus {

    /**
     * This status means that the user is not logged in
     * @param isSignOut Whether this status was caused by a sign-out
     */
    data class NotAuthenticated(
        val isSignOut: Boolean = false,
    ) : SessionStatus

    /**
     * This status means that [Auth] is currently initializing the session
     */
    data object Initializing : SessionStatus

    /**
     * This status means the session expired and [Auth] failed to refresh it. This does not mean the user is signed out, you just cannot do authenticated requests, until the refresh went through.
     */
    data object SessionExpired : SessionStatus

    /**
     * This status means that [Auth] holds a valid session
     * @param session The session
     */
    data class Authenticated(
        val session: UserSession,
        val flag: SessionFlag
    ): SessionStatus

}