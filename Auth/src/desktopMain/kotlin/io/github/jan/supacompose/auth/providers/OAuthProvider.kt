package io.github.jan.supacompose.auth.providers

import io.github.aakira.napier.Napier
import io.github.jan.supacompose.SupabaseClient
import io.github.jan.supacompose.auth.auth
import io.github.jan.supacompose.auth.user.UserSession
import io.ktor.http.ContentType
import io.ktor.server.application.call
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.http.content.file
import io.ktor.server.http.content.static
import io.ktor.server.http.content.staticRootFolder
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.awt.Desktop
import java.io.File
import java.net.URI
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

actual abstract class OAuthProvider : AuthProvider<ExternalAuthConfig, Unit> {

    actual abstract fun provider(): String

    actual override suspend fun login(
        supabaseClient: SupabaseClient,
        onSuccess: suspend (UserSession) -> Unit,
        redirectUrl: String?,
        config: (ExternalAuthConfig.() -> Unit)?
    ) {
        withContext(Dispatchers.IO) {
            launch {
                val authConfig = ExternalAuthConfig().apply {
                    config?.invoke(this)
                }
                createServer(authConfig, supabaseClient, redirectUrl, onSuccess, authConfig.onFail)
            }
        }
    }

    actual override suspend fun signUp(
        supabaseClient: SupabaseClient,
        onSuccess: suspend (UserSession) -> Unit,
        redirectUrl: String?,
        config: (ExternalAuthConfig.() -> Unit)?
    ) = login(supabaseClient, onSuccess, redirectUrl, config = config)

    private suspend fun createServer(config: ExternalAuthConfig, supabaseClient: SupabaseClient, redirectUrl: String?, onSuccess: suspend (UserSession) -> Unit, onFail: (AuthFail) -> Unit) {
        if(redirectUrl != null) {
            Desktop.getDesktop().browse(URI(supabaseClient.supabaseHttpUrl + "/auth/v1/authorize?provider=${provider()}&redirect_to=$redirectUrl"))
            return
        }
        coroutineScope {
            launch {
                var done = false
                val mutex = Mutex()
                embeddedServer(CIO, port = config.httpPort, parentCoroutineContext = coroutineContext) {
                    routing {
                        static("/") {
                            staticRootFolder = File(this::class.java.classLoader.getResource("templates").path)
                            file("index.html")
                        }
                        get("/callback") {
                            Napier.d {
                                "Received callback on oauth callback"
                            }
                            val accessToken = call.request.queryParameters["access_token"] ?: return@get call.respondText("No access token")
                            val refreshToken = call.request.queryParameters["refresh_token"] ?: return@get call.respondText("No refresh token")
                            val expiresIn = call.request.queryParameters["expires_in"]?.toLong() ?: return@get call.respondText("No expires in")
                            val tokenType = call.request.queryParameters["token_type"] ?: return@get call.respondText("No token type")
                            val type = call.request.queryParameters["type"] ?: ""
                            launch {
                                val user = supabaseClient.auth.getUser(accessToken)
                                onSuccess(UserSession(accessToken, refreshToken, expiresIn, tokenType, user, type))
                            }
                            Napier.d {
                                "Successfully received callback on oauth callback"
                            }
                            mutex.withLock {
                                done = true
                            }
                            call.respondText(ContentType.Text.Html) {
                                """
                                    <!DOCTYPE html>
                                    <html>
                                        <head>
                                            <meta charset="utf-8">
                                            <link rel="icon" href="${config.htmlIconUrl}">
                                            <title>${config.htmlTitle}</title>
                                        </head>
                                        <body style="background-color:#2f3237;">
                                            <p style="    
                                                position: absolute;
                                                top: 50%;
                                                font-size: 2.5em;
                                                left: 50%;
                                                -moz-transform: translateX(-50%) translateY(-50%);
                                                -webkit-transform: translateX(-50%) translateY(-50%);
                                                transform: translateX(-50%) translateY(-50%);
                                                font-family: Uni Sans, sans-serif;
                                            ">${config.htmlText}</p>
                                        </body>
                                    </html>
                                """.trimIndent()
                            }
                        }
                    }
                }.start(wait = false).also {
                    val port = it.resolvedConnectors().first().port
                    Desktop.getDesktop().browse(URI(supabaseClient.supabaseHttpUrl + "/auth/v1/authorize?provider=${provider()}&redirect_to=http://localhost:${port}/index.html"))
                    delay(config.timeout.inWholeMilliseconds)
                    it.stop()
                    if(!done) {
                        onFail(AuthFail.Timeout)
                    }
                }
            }
        }
    }

}

var ExternalAuthConfig.httpPort: Int
    get() = params["httpPort"] as? Int ?: 0
    set(value) {
        params["httpPort"] = value
    }

var ExternalAuthConfig.timeout: Duration
    get() = params["timeout"] as? Duration ?: 1.minutes
    set(value) {
        params["timeout"] = value
    }

var ExternalAuthConfig.htmlTitle: String
    get() = params["htmlTitle"] as? String ?: "SupaCompose"
    set(value) {
        params["htmlTitle"] = value
    }

var ExternalAuthConfig.htmlText: String
    get() = params["htmlText"] as? String ?: "Logged in. You may continue in your app."
    set(value) {
        params["htmlText"] = value
    }

var ExternalAuthConfig.htmlIconUrl: String
    get() = params["htmlIconUrl"] as? String ?: "https://supabase.com/brand-assets/supabase-logo-icon.png"
    set(value) {
        params["htmlIconUrl"] = value
    }

internal val ExternalAuthConfig.onFail
    get() = params["onFail"] as? ((AuthFail) -> Unit) ?: {}

fun ExternalAuthConfig.onFail(onFail: (AuthFail) -> Unit) {
    params["onFail"] = onFail
}
