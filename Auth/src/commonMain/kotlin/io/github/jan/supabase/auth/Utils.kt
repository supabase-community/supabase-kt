package io.github.jan.supabase.auth

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.auth.status.NotAuthenticatedReason
import io.github.jan.supabase.auth.status.SessionSource
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserSession
import io.github.jan.supabase.logging.d
import io.ktor.client.request.HttpRequestBuilder
import kotlinx.coroutines.launch

@SupabaseInternal
fun Auth.parseFragmentAndImportSession(fragment: String, onSessionSuccess: (UserSession) -> Unit = {}) {
    Auth.logger.d { "Parsing deeplink fragment $fragment" }
    val error = checkForErrorHash(fragment)
    if(error != null) {
        Auth.logger.d { "Error found in fragment: $error" }
        setSessionStatus(SessionStatus.NotAuthenticated(false, error))
        return
    }
    val session = parseSessionFromFragment(fragment)
    this as AuthImpl
    authScope.launch {
        val user = retrieveUser(session.accessToken)
        val newSession = session.copy(user = user)
        onSessionSuccess(newSession)
        importSession(newSession, source = SessionSource.External)
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

internal fun getFragmentParts(fragment: String) = fragment.split("&").associate {
    it.split("=").let { pair ->
        pair[0] to pair[1]
    }
}

@SupabaseInternal
fun HttpRequestBuilder.redirectTo(url: String) {
    this.url.parameters["redirect_to"] = url
}

internal fun invalidArg(message: String): Nothing = throw IllegalArgumentException(message)

internal expect suspend fun SupabaseClient.openExternalUrl(url: String)

internal expect suspend fun Auth.startExternalAuth(
    redirectUrl: String?,
    getUrl: suspend (redirectTo: String?) -> String,
    onSessionSuccess: suspend (UserSession) -> Unit
)