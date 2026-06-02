package io.github.jan.supabase.auth.status

import io.github.jan.supabase.exceptions.RestException

/**
 * Represents the cause of a refresh error
 */
sealed interface RefreshFailureCause {

    /**
     * The refresh failed due to a network error
     * @param exception The exception that caused the error
     */
    data class NetworkError(val exception: Throwable) : RefreshFailureCause

    /**
     * The refresh failed due to an internal server error
     * @param exception The rest exception that caused the error
     */
    data class InternalServerError(val exception: RestException) : RefreshFailureCause

}