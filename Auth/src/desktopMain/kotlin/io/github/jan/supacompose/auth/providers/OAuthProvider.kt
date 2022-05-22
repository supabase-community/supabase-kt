package io.github.jan.supacompose.auth.providers

import com.soywiz.korio.async.delay
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.awt.Desktop
import java.io.File
import java.net.URI

actual abstract class OAuthProvider : AuthProvider<ExternalAuthConfig, Unit> {

    actual abstract fun provider(): String

    actual override suspend fun login(
        supabaseClient: SupabaseClient,
        onSuccess: suspend (UserSession) -> Unit,
        onFail: (OAuthFail) -> Unit,
        credentials: (ExternalAuthConfig.() -> Unit)?
    ) {
        withContext(Dispatchers.IO) {
            launch {
                val config = ExternalAuthConfig().apply {
                    credentials?.invoke(this)
                }
                createServer(config, supabaseClient, onSuccess, onFail)
            }
        }
    }

    actual override suspend fun signUp(supabaseClient: SupabaseClient, credentials: ExternalAuthConfig.() -> Unit) {
        TODO("Not yet implemented")
    }

    private suspend fun createServer(config: ExternalAuthConfig, supabaseClient: SupabaseClient, onSuccess: suspend (UserSession) -> Unit, onFail: (OAuthFail) -> Unit) {
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
                            val accessToken = call.request.queryParameters["access_token"] ?: return@get call.respondText("No access token")
                            val refreshToken = call.request.queryParameters["refresh_token"] ?: return@get call.respondText("No refresh token")
                            val expiresIn = call.request.queryParameters["expires_in"]?.toInt() ?: return@get call.respondText("No expires in")
                            launch {
                                val user = supabaseClient.auth.goTrueClient.getUser(accessToken)
                                onSuccess(UserSession(accessToken, refreshToken, expiresIn, "", user))
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
                    Desktop.getDesktop().browse(URI(supabaseClient.supabaseUrl + "/auth/v1/authorize?provider=${provider()}&redirect_to=http://localhost:${port}/index.html"))
                    delay(config.timeout)
                    it.stop()
                    if(!done) {
                        onFail(OAuthFail.Timeout)
                    }
                }
            }
        }
    }

}