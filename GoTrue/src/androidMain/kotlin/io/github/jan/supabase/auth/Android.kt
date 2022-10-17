package io.github.jan.supabase.auth

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import io.github.aakira.napier.Napier
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotiations.SupaComposeInternal
import io.github.jan.supabase.auth.user.UserSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(SupaComposeInternal::class)
internal fun GoTrue.openOAuth(provider: String) {
    this as GoTrueImpl
    val deepLink = "${config.scheme}://${config.host}"
    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(supabaseClient.supabaseHttpUrl + "/auth/v1/authorize?provider=${provider}&redirect_to=$deepLink"))
    config.activity.startActivity(browserIntent)
}

@SupaComposeInternal
var GoTrue.Config.activity: Activity
    get() = params["activity"] as? Activity ?: throw IllegalStateException("Use initializeAndroid on your onCreate method")
    set(value) {
        params["activity"] = value
    }

var GoTrue.Config.scheme: String
    get() = (params["scheme"] as? String) ?: "supacompose"
    set(value) {
        params["scheme"] = value
    }

var GoTrue.Config.host: String
    get() = (params["host"] as? String) ?: "login"
    set(value) {
        params["host"] = value
    }

//add a contextual receiver later in kotlin 1.7 and remove the supabaseClient parameter
@OptIn(SupaComposeInternal::class)
fun Activity.initializeAndroid(supabaseClient: SupabaseClient, onSessionSuccess: (UserSession) -> Unit = {}) {
    val authPlugin = supabaseClient.pluginManager.getPlugin<GoTrueImpl>("auth")
    authPlugin.config.activity = this
    addLifecycleCallbacks(authPlugin)

    val data = intent?.data ?: return
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
            authPlugin.startJob(session)
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

private fun addLifecycleCallbacks(authPlugin: GoTrueImpl) {
    if(!authPlugin.config.autoLoadFromStorage) return
    val lifecycle = ProcessLifecycleOwner.get().lifecycle
    val scope = CoroutineScope(Dispatchers.IO)
    lifecycle.addObserver(
        object : DefaultLifecycleObserver {

            override fun onStart(owner: LifecycleOwner) {
                scope.launch {
                    Napier.d {
                        "Trying to load the latest session"
                    }
                    authPlugin._status.value = GoTrue.Status.LOADING_FROM_STORAGE
                    val userSession = authPlugin.sessionManager.loadSession()
                    if(userSession != null) {
                        Napier.d {
                            "Successfully loaded session from storage"
                        }
                        authPlugin.startJob(userSession)
                    } else {
                        authPlugin._status.value = GoTrue.Status.NOT_AUTHENTICATED
                    }
                }
            }
            override fun onStop(owner: LifecycleOwner) {
                Napier.d { "Cancelling session job because app is switching to the background" }
                authPlugin.sessionJob?.cancel()
            }
        }
    )
}