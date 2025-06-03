package io.github.jan.supabase.auth

import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.auth.event.AuthEvent
import io.github.jan.supabase.auth.status.SessionSource
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserSession
import io.github.jan.supabase.buildUrl
import io.github.jan.supabase.logging.d
import io.ktor.client.request.HttpRequestBuilder
import kotlinx.coroutines.launch

@SupabaseInternal
fun Auth.parseFragmentAndImportSession(fragment: String, onFinish: (UserSession?) -> Unit = {}) {
    Auth.logger.d { "Parsing fragment $fragment" }
    val parameters = getFragmentParts(fragment)
    if(handledUrlParameterError { parameters[it] }) {
        onFinish(null)
        return
    }
    val session = try {
        parseSessionFromFragment(fragment)
    } catch(e: IllegalArgumentException) {
        Auth.logger.d(e) { "Received invalid session fragment. Ignoring." }
        return
    }
    this as AuthImpl
    authScope.launch {
        val user = retrieveUser(session.accessToken)
        val newSession = session.copy(user = user)
        onFinish(newSession)
        importSession(newSession, source = SessionSource.External)
    }
}

internal fun getFragmentParts(fragment: String) = fragment.split("&").associate {
    it.split("=").let { pair ->
        pair[0] to pair[1]
    }
}

internal fun checkForUrlParameterError(parameters: (String) -> String?): AuthEvent.OtpError? {
    val error = parameters("error")
    val errorCode = parameters("error_code")
    val errorDescription = parameters("error_description")
    return if(errorCode != null) {
        AuthEvent.OtpError(
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

@SupabaseInternal
fun HttpRequestBuilder.redirectTo(url: String) {
    this.url.parameters["redirect_to"] = url
}

internal fun consumeHashParameters(parameters: List<String>, url: String): String {
    return buildUrl(url) {
        fragment = fragment.split("&").filter {
            it.split("=").first() !in parameters
        }.joinToString("&")
    }
}

internal fun consumeUrlParameter(parameters: List<String>, url: String): String {
    return buildUrl(url) {
        parameters.forEach { parameter ->
            this.parameters.remove(parameter)
        }
    }
}