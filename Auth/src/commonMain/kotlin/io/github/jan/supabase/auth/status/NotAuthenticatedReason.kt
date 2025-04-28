package io.github.jan.supabase.auth.status

import io.github.jan.supabase.auth.exception.AuthErrorCode

/**
 * Represents the reason for an [SessionStatus.NotAuthenticated] status.
 */
sealed interface NotAuthenticatedReason {

    /**
     * This status means that there was an error while trying to authenticate the user, e.g. from external authentication providers like OAuth. Use this to notify the user about the error.
     * You can use [errorCode] to further handle the error.
     * @param error The raw error code from the server.
     * @param errorDescription The description of the error.
     */
    data class Error(val error: String, val errorDescription: String) : NotAuthenticatedReason {

        /**
         * The error code of the rest exception. If [error] is not a known [AuthErrorCode], this will be null. Then, use [error] instead to get the raw unknown error code.
         */
        val errorCode: AuthErrorCode? = AuthErrorCode.fromValue(error)

    }

    /**
     * This status means that the user was just signed out.
     */
    data object SignOut: NotAuthenticatedReason

    /**
     * This status means that there was no session found in the local storage.
     */
    data object SessionNotFound: NotAuthenticatedReason

}