package io.github.jan.supabase.gotrue

import co.touchlab.kermit.Logger
import io.github.jan.supabase.annotiations.SupabaseInternal
import io.github.jan.supabase.gotrue.user.UserSession
import io.ktor.client.request.HttpRequestBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@SupabaseInternal
fun GoTrue.parseFragmentAndImportSession(fragment: String, onSessionSuccess: (UserSession) -> Unit = {}) {
    Logger.d { "Parsing deeplink fragment" }
    val map = fragment.split("&").associate {
        it.split("=").let { pair ->
            pair[0] to pair[1]
        }
    }
    Logger.d { "Fragment parts: $map" }

    val accessToken = map["access_token"] ?: return
    val refreshToken = map["refresh_token"] ?: return
    val expiresIn = map["expires_in"]?.toLong() ?: return
    val tokenType = map["token_type"] ?: return
    val type = map["type"] ?: ""
    val providerToken = map["provider_token"]
    val providerRefreshToken = map["provider_refresh_token"]
    val scope = CoroutineScope(Dispatchers.Default)
    Logger.d {
        "Received session deeplink"
    }
    scope.launch {
        val user = retrieveUser(accessToken)
        val session = UserSession(accessToken, refreshToken, providerRefreshToken, providerToken, expiresIn, tokenType, user, type)
        onSessionSuccess(session)
        importSession(session)
    }
}

@SupabaseInternal
fun HttpRequestBuilder.redirectTo(url: String) {
    this.url.parameters["redirect_to"] = url
}