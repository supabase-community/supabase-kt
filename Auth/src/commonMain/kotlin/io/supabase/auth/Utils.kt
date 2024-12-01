package io.supabase.auth

import io.supabase.SupabaseClient
import io.supabase.annotations.SupabaseInternal
import io.supabase.auth.status.SessionSource
import io.supabase.auth.user.UserSession
import io.supabase.logging.d
import io.ktor.client.request.HttpRequestBuilder
import kotlinx.coroutines.launch

@SupabaseInternal
fun Auth.parseFragmentAndImportSession(fragment: String, onSessionSuccess: (UserSession) -> Unit = {}) {
    Auth.logger.d { "Parsing deeplink fragment $fragment" }
    val session = parseSessionFromFragment(fragment)
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

internal fun invalidArg(message: String): Nothing = throw IllegalArgumentException(message)

internal expect suspend fun SupabaseClient.openExternalUrl(url: String)

internal expect suspend fun Auth.startExternalAuth(
    redirectUrl: String?,
    getUrl: suspend (redirectTo: String?) -> String,
    onSessionSuccess: suspend (UserSession) -> Unit
)