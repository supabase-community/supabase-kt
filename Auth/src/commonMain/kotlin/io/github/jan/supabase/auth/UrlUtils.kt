package io.github.jan.supabase.auth

import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.auth.status.SessionSource
import io.github.jan.supabase.auth.user.UserSession
import io.github.jan.supabase.buildUrl
import io.github.jan.supabase.logging.d
import io.ktor.client.request.HttpRequestBuilder
import kotlinx.coroutines.launch

@SupabaseInternal
fun Auth.parseFragmentAndImportSession(fragment: String, onSessionSuccess: (UserSession) -> Unit = {}) {
    Auth.logger.d { "Parsing deeplink fragment $fragment" }
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
        onSessionSuccess(newSession)
        importSession(newSession, source = SessionSource.External)
    }
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