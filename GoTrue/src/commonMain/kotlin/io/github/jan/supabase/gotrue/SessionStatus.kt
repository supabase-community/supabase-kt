package io.github.jan.supabase.gotrue

import io.github.jan.supabase.gotrue.providers.AuthProvider
import io.github.jan.supabase.gotrue.user.UserSession

/**
 * Represents the status of the current session in [Auth]
 */
sealed interface SessionStatus {

    /**
     * This status means that the user is not logged in
     * @param isSignOut Whether this status was caused by a sign out
     */
    data class NotAuthenticated(val isSignOut: Boolean) : SessionStatus

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
     * @param source The source of the session
     */
    data class Authenticated(val session: UserSession, val source: SessionSource?) : SessionStatus

}

sealed interface SessionSource {
    data object Storage : SessionSource
    data class SignIn(val provider: AuthProvider<*, *>) : SessionSource
    data class SignUp(val provider: AuthProvider<*, *>) : SessionSource
    data object External : SessionSource
    data class Refresh(val oldSession: UserSession) : SessionSource
    data class UserChanged(val oldSession: UserSession) : SessionSource
    data class UserIdentitiesChanged(val oldSession: UserSession) : SessionSource
}