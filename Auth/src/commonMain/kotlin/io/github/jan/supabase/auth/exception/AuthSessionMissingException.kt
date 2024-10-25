package io.github.jan.supabase.auth.exception

/**
 * Exception thrown when a session is not found.
 */
class AuthSessionMissingException(statusCode: Int): AuthRestException(
    errorCode = CODE,
    statusCode = statusCode,
    message = "Session not found. This can happen if the user was logged out or deleted."
) {

    internal companion object {
        const val CODE = "session_not_found"
    }

}