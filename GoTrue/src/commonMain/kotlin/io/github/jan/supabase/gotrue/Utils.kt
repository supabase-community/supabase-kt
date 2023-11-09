package io.github.jan.supabase.gotrue

import co.touchlab.kermit.Logger
import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.gotrue.user.UserSession
import io.ktor.client.request.HttpRequestBuilder
import kotlinx.coroutines.launch

@SupabaseInternal
fun Auth.parseFragmentAndImportSession(fragment: String, onSessionSuccess: (UserSession) -> Unit = {}) {
    Logger.d { "Parsing deeplink fragment $fragment" }
    val session = parseSessionFromFragment(fragment)
    this as AuthImpl
    authScope.launch {
        val user = retrieveUser(session.accessToken)
        val newSession = session.copy(user = user)
        onSessionSuccess(newSession)
        importSession(newSession)
    }
}

@SupabaseInternal
fun HttpRequestBuilder.redirectTo(url: String) {
    this.url.parameters["redirect_to"] = url
}

internal fun invalidArg(message: String): Nothing = throw IllegalArgumentException(message)