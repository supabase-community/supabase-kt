package io.github.jan.supabase.auth.url

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.event.AuthEvent
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.logging.d

internal fun checkForUrlParameterError(parameters: (String) -> String?): AuthEvent.ErrorCodeReceived? {
    val error = parameters("error")
    val errorCode = parameters("error_code")
    val errorDescription = parameters("error_description")
    return if(errorCode != null) {
        AuthEvent.ErrorCodeReceived(
            error = errorCode,
            errorDescription = "$errorDescription ($error)",
        )
    } else null
}

internal fun Auth.handledUrlParameterError(parameters: (String) -> String?): Boolean {
    val error = checkForUrlParameterError(parameters)
    return if(error != null) {
        if(sessionStatus.value !is SessionStatus.Authenticated) {
            Auth.logger.d { "Found error code in the URL Parameters: $error. Emitting event..." }
            emitEvent(error)
        }
        true
    } else false
}