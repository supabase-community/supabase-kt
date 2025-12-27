package io.github.jan.supabase.auth.event

import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.exception.AuthErrorCode
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.auth.status.RefreshFailureCause

/**
 * This interface represents the events that can be emitted by [Auth].
 *
 * In comparison to [io.github.jan.supabase.auth.status.SessionStatus], these events can happen independently of the session status.
 */
@SupabaseExperimental
sealed interface AuthEvent {

    /**
     * This event indicates that an authentication error code was received, for example from
     * external authentication providers like OAuth.
     *
     * The error code may appear in a deeplink on mobile targets (when handled via `handleDeeplinks`), an HTTP callback on desktop
     * targets, or in the current URL on web targets. This is not to be confused with
     * [AuthRestException], which is only thrown when an authentication request returns
     * an error response.
     *
     * You can use [errorCode] to further handle the error.
     *
     * @param error The raw error code from the server.
     * @param errorDescription The description of the error.
     */
    data class ErrorCodeReceived(val error: String, val errorDescription: String) : AuthEvent {

        /**
         * The error code of the rest exception. If [error] is not a known [AuthErrorCode], this will be null. Then, use [error] instead to get the raw unknown error code.
         */
        val errorCode: AuthErrorCode? = AuthErrorCode.fromValue(error)

    }

    /**
     * This event means that [Auth] had an error while refreshing the session, but will try to refresh it again.
     *
     * This event can be emitted even if the session has not expired yet because [Auth] tries to refresh the session before it expires.
     *
     * @param cause The cause of the error
     */
    data class RefreshFailure(
        val cause: RefreshFailureCause
    ) : AuthEvent

}