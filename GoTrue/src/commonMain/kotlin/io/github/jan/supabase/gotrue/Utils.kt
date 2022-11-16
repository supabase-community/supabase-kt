package io.github.jan.supabase.gotrue

import io.github.aakira.napier.Napier
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.user.UserSession
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

suspend fun HttpResponse.checkErrors(error: String = "Error while performing request"): HttpResponse {
    if(status.value !in 200..299) {
        throw Exception("da")
      //  throw RestException(status.value, error, bodyAsText(), headers.entries().flatMap { (key, value) -> listOf(key) + value })
    }
    return this
}

internal fun SupabaseClient.parseFragment(fragment: String, onSessionSuccess: (UserSession) -> Unit = {}) {
    val authPlugin = gotrue
    val map = fragment.split("&").associate {
        it.split("=").let { pair ->
            pair[0] to pair[1]
        }
    }

    val accessToken = map["access_token"] ?: return
    val refreshToken = map["refresh_token"] ?: return
    val expiresIn = map["expires_in"]?.toLong() ?: return
    val tokenType = map["token_type"] ?: return
    val type = map["type"] ?: ""
    val scope = CoroutineScope(Dispatchers.Default)
    Napier.d {
        "Received session deeplink"
    }
    scope.launch {
        val user = authPlugin.getUser(accessToken)
        val session = UserSession(accessToken, refreshToken, expiresIn, tokenType, user, type)
        onSessionSuccess(session)
        authPlugin.startAutoRefresh(session)
    }
}