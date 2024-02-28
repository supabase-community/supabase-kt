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
    data class Authenticated(val session: UserSession, val source: SessionSource = SessionSource.Unknown) : SessionStatus {

        /**
         * Whether the session is new, i.e. [source] is [SessionSource.SignIn], [SessionSource.SignUp] or [SessionSource.External].
         * Use this to determine whether this status is the result of a new sign in or sign up or just a session refresh.
         */
        val isNew: Boolean
            get() = source is SessionSource.SignIn || source is SessionSource.SignUp || source is SessionSource.External

    }

}

/**
 * Represents the source of a session
 */
sealed interface SessionSource {

    /**
     * The session was loaded from storage
     */
    data object Storage : SessionSource

    /**
     * The session was loaded from a sign in
     * @param provider The provider that was used to sign in
     */
    data class SignIn(val provider: AuthProvider<*, *>) : SessionSource

    /**
     * The session was loaded from a sign up (only if auto-confirm is enabled)
     * @param provider The provider that was used to sign up
     */
    data class SignUp(val provider: AuthProvider<*, *>) : SessionSource

    /**
     * The session comes from an external source, e.g. OAuth via deeplinks.
     */
    data object External : SessionSource

    /**
     * The session comes from an unknown source
     */
    data object Unknown : SessionSource

    /**
     * The session was refreshed
     * @param oldSession The old session
     */
    data class Refresh(val oldSession: UserSession) : SessionSource

    /**
     * The session was changed due to a user change (e.g. via [Auth.modifyUser] or [Auth.retrieveUserForCurrentSession])
     * @param oldSession The old session
     */
    data class UserChanged(val oldSession: UserSession) : SessionSource

    /**
     * The session was changed due to a user identity change (e.g. via [Auth.linkIdentity] or [Auth.unlinkIdentity])
     * @param oldSession The old session
     */
    data class UserIdentitiesChanged(val oldSession: UserSession) : SessionSource
}