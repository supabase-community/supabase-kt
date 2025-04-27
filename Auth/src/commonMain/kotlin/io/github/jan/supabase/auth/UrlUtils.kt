package io.github.jan.supabase.auth

import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.auth.status.NotAuthenticatedReason
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
    val error = checkForErrorHash(fragment)
    if(error != null) {
        Auth.logger.d { "Error found in fragment: $error" }
        setSessionStatus(SessionStatus.NotAuthenticated(false, error))
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

internal fun checkForErrorHash(fragment: String): NotAuthenticatedReason.Error? {
    val parameters = getFragmentParts(fragment)
    val error = parameters["error"]
    val errorCode = parameters["error_code"]
    val errorDescription = parameters["error_description"]
    return if(errorCode != null) {
        NotAuthenticatedReason.Error(
            error = errorCode,
            errorDescription = "$errorDescription ($error)",
        )
    } else null
}

internal fun checkForUrlParameterError(parameters: (String) -> String?): NotAuthenticatedReason.Error? {
    val error = parameters("error")
    val errorCode = parameters("error_code")
    val errorDescription = parameters("error_description")
    return if(errorCode != null) {
        NotAuthenticatedReason.Error(
            error = errorCode,
            errorDescription = "$errorDescription ($error)",
        )
    } else null
}

internal fun Auth.handledUrlParameterError(parameters: (String) -> String?): Boolean {
    val error = checkForUrlParameterError(parameters)
    return if(error != null) {
        Auth.logger.d { "Found error code in the URL Parameters: $error. Updating session status..." }
        setSessionStatus(SessionStatus.NotAuthenticated(false, error))
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