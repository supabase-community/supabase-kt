package io.github.jan.supacompose.auth

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import io.github.jan.supacompose.SupabaseClient
import io.github.jan.supacompose.annotiations.SupaComposeInternal
import io.github.jan.supacompose.auth.user.UserSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

@OptIn(SupaComposeInternal::class)
internal fun Auth.openOAuth(provider: String) {
    this as AuthImpl
    val deepLink = "${config.scheme}://${config.host}"
    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(supabaseClient.supabaseHttpUrl + "/auth/v1/authorize?provider=${provider}&redirect_to=$deepLink"))
    config.activity.startActivity(browserIntent)
}

@SupaComposeInternal
var Auth.Config.activity: Activity
    get() = params["activity"] as? Activity ?: throw IllegalStateException("Use initializeAndroid on your onCreate method")
    set(value) {
        params["activity"] = value
    }

var Auth.Config.scheme: String
    get() = (params["scheme"] as? String) ?: "supacompose"
    set(value) {
        params["scheme"] = value
    }

var Auth.Config.host: String
    get() = (params["host"] as? String) ?: "login"
    set(value) {
        params["host"] = value
    }

internal var Auth.Config.sessionFile: File?
    get() = (params["sessionFile"] as? File)
    set(value) {
        if(value == null) {
            params.remove("sessionFile")
            return
        }
        params["sessionFile"] = value
    }


//add a contextual receiver later in kotlin 1.7 and remove the supabaseClient parameter
@OptIn(SupaComposeInternal::class)
fun Activity.initializeAndroid(supabaseClient: SupabaseClient, onSessionSuccess: (UserSession) -> Unit = {}) {

    val authPlugin = supabaseClient.plugins["auth"] as? AuthImpl ?: throw IllegalStateException("You need to install the Auth plugin on the supabase client to handle deep links")
    authPlugin.config.activity = this
    authPlugin.config.sessionFile = File(filesDir, "session.json")
    addLifecycleCallbacks(supabaseClient, authPlugin)

    Napier.base(DebugAntilog())
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

    /*fun handleErrorDeeplink() {
        val errorCode = map["error_code"]?.toInt() ?: return
        val description = map["error_description"] ?: ""
        onAuthFail(AuthFail.RedirectError(errorCode, description))
    }*/

    when {
       // "error_code" in map -> handleErrorDeeplink()
        "access_token" in map -> handleSessionDeeplink()
    }
}

private fun addLifecycleCallbacks(supabaseClient: SupabaseClient, authPlugin: AuthImpl) {
    val lifecycle = ProcessLifecycleOwner.get().lifecycle
    lifecycle.addObserver(
        object : DefaultLifecycleObserver {

            override fun onStart(owner: LifecycleOwner) {
                supabaseClient.launch {
                    Napier.d {
                        "Trying to load the latest session"
                    }
                    authPlugin._status.value = Auth.Status.LOADING_FROM_STORAGE
                    val userSession = authPlugin.sessionManager.loadSession(supabaseClient, authPlugin)
                    if(userSession != null) {
                        Napier.d {
                            "Successfully loaded session from storage"
                        }
                        authPlugin.startJob(userSession)
                    } else {
                        authPlugin._status.value = Auth.Status.NOT_AUTHENTICATED
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