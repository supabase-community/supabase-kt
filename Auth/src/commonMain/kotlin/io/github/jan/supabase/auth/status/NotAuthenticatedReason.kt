package io.github.jan.supabase.auth.status

import io.github.jan.supabase.auth.exception.AuthErrorCode

/**
 * Represents the reason why a user is not authenticated.
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
     * This status means that the user is not logged in because the session was deleted by the user
     */
    data object SignOut: NotAuthenticatedReason

    /**
     * This status means that the user is not logged in because there was no session found
     */
    data object SessionNotFound: NotAuthenticatedReason

    /**
     * This status means that the reason for the user not being logged in is unknown
     */
    data object Unknown: NotAuthenticatedReason

}