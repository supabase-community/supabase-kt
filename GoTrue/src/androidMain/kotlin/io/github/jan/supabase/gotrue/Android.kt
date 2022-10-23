package io.github.jan.supabase.gotrue

import android.app.Activity
import android.content.Intent
import android.net.Uri
import io.github.aakira.napier.Napier
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotiations.SupabaseInternal
import io.github.jan.supabase.gotrue.user.UserSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

internal fun GoTrue.openOAuth(provider: String) {
    this as GoTrueImpl
    val deepLink = "${config.scheme}://${config.host}"
    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(supabaseClient.supabaseHttpUrl + "/auth/v1/authorize?provider=${provider}&redirect_to=$deepLink"))
    browserIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    applicationContext().startActivity(browserIntent)
}

var GoTrue.Config.scheme: String
    get() = (params["scheme"] as? String) ?: "Supabase"
    set(value) {
        params["scheme"] = value
    }

var GoTrue.Config.host: String
    get() = (params["host"] as? String) ?: "login"
    set(value) {
        params["host"] = value
    }


//TODO: Add context receiver 'Activity'
fun SupabaseClient.handleDeeplinks(intent: Intent, onSessionSuccess: (UserSession) -> Unit = {}) {
    val authPlugin = pluginManager.getPlugin(GoTrue)
    val data = intent.data ?: return
    val scheme = data.scheme ?: return
    val host = data.host ?: return
    if(scheme != authPlugin.config.scheme || host != authPlugin.config.host) return
    val fragment = data.fragment ?: return
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
    val scope = CoroutineScope(Dispatchers.IO)
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

@Deprecated("initializeAndroid is no longer mandatory for android. If you want to handle deeplinks, use SupabaseClient.handleDeeplinks(intent) instead", ReplaceWith("supabaseClient.handleDeeplinks(intent)"), DeprecationLevel.ERROR)
fun Activity.initializeAndroid(supabaseClient: SupabaseClient, onSessionSuccess: (UserSession) -> Unit = {}) {
    val authPlugin = supabaseClient.pluginManager.getPlugin(GoTrue)
   // authPlugin.config.activity = this
   // addLifecycleCallbacks(authPlugin)

    val data = intent.data ?: return
    val scheme = data.scheme ?: return
    val host = data.host ?: return
    if(scheme != authPlugin.config.scheme || host != authPlugin.config.host) return
    val fragment = data.fragment ?: return
    val map = fragment.split("&").associate {
        it.split("=").let { pair ->
            pair[0] to pair[1]
        }
    }

    fun handleSessionDeeplink() {
        val accessToken = map["access_token"] ?: return
        val refreshToken = map["refresh_token"] ?: return
        val expiresIn = map["expires_in"]?.toLong() ?: return
        val tokenType = map["token_type"] ?: return
        val type = map["type"] ?: ""
        val scope = CoroutineScope(Dispatchers.IO)
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

    /**fun handleErrorDeeplink() {
        val errorCode = map["error_code"]?.toInt() ?: return
        val description = map["error_description"] ?: ""
        onAuthFail(AuthFail.RedirectError(errorCode, description))
    }*/

    when {
       // "error_code" in map -> handleErrorDeeplink()
        "access_token" in map -> handleSessionDeeplink()
    }
}