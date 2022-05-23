package io.github.jan.supacompose.auth

import android.app.Activity
import androidx.activity.ComponentActivity
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import android.net.Uri
import io.github.jan.supacompose.SupabaseClient
import io.github.jan.supacompose.plugins.SupabasePlugin
import io.github.jan.supacompose.annotiations.SupaComposeInternal
import io.github.jan.supacompose.auth.user.UserInfo
import io.github.jan.supacompose.auth.user.UserSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class DeepLinks(private val supabaseClient: SupabaseClient, val config: Config) {

    class Config(var scheme: String = "supacompose", var host: String = "login") {

        @SupaComposeInternal
        lateinit var activity: Activity

    }

    @OptIn(SupaComposeInternal::class)
    fun openOAuth(provider: String) {
        val deepLink = "${config.scheme}://${config.host}"
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(supabaseClient.supabaseUrl + "/auth/v1/authorize?provider=${provider}&redirect_to=$deepLink").also(::println))
        config.activity.startActivity(browserIntent)
    }

    companion object : SupabasePlugin<Config, DeepLinks> {

        override val key = "deeplinks"

        override fun create(supabaseClient: SupabaseClient, config: Config.() -> Unit) = DeepLinks(supabaseClient, Config().apply(config))

    }

}

//add a contextual receiver later in kotlin 1.7 and remove the supabaseClient parameter
@OptIn(SupaComposeInternal::class)
fun Activity.handleDeepLinks(supabaseClient: SupabaseClient) {
    val plugin = (supabaseClient.plugins["deeplinks"] as? DeepLinks ?: throw IllegalStateException("You need to install the DeepLink plugin on the supabase client to handle deep links"))
    val authPlugin = supabaseClient.plugins["auth"] as? AuthImpl ?: throw IllegalStateException("You need to install the Auth plugin on the supabase client to handle deep links")
    plugin.config.activity = this
    val data = intent?.data ?: return
    val scheme = data.scheme ?: return
    val host = data.host ?: return
    if(scheme != plugin.config.scheme || host != plugin.config.host) return
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
    val scope = CoroutineScope(Dispatchers.IO)
    scope.launch {
        val user = authPlugin.goTrueClient.getUser(accessToken)
        authPlugin.startJob(UserSession(accessToken, refreshToken, expiresIn, tokenType, user))
    }
}
