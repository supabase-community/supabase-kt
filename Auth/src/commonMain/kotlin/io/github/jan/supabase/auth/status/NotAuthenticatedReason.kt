package io.github.jan.supabase.auth.status

import io.github.jan.supabase.auth.exception.AuthRestException
import kotlin.jvm.JvmInline

/**
 * Represents the reason why a user is not authenticated.
 */
sealed interface NotAuthenticatedReason {

    /**
     * This status means that the user is not logged in
     */
    @JvmInline
    value class Error(val error: AuthRestException) : NotAuthenticatedReason {
        override fun toString(): String = "Error(error=$error)"
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