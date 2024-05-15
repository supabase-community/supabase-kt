package io.github.jan.supabase.gotrue.exception

/**
 * Exception thrown when a session is not found.
 */
class AuthSessionMissingException: AuthRestException(
    errorCode = CODE,
    message = "Session not found. This can happen if the user was logged out or deleted."
) {

    companion object {
        const val CODE = "session_not_found"
    }

}