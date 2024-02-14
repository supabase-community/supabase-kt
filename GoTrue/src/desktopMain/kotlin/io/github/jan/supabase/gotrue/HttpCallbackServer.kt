package io.github.jan.supabase.gotrue

import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.gotrue.user.UserSession
import io.github.jan.supabase.logging.d
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.ApplicationStopPreparing
import io.ktor.server.application.call
import io.ktor.server.cio.CIO
import io.ktor.server.engine.ApplicationEngineEnvironment
import io.ktor.server.engine.embeddedServer
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

@OptIn(SupabaseExperimental::class)
internal suspend fun createServer(
    url: suspend (redirect: String) -> String,
    auth: Auth,
    onSuccess: suspend (UserSession) -> Unit
) {
    auth as AuthImpl
    Auth.logger.d { "Creating OAuth callback server" }
    val server = embeddedServer(CIO, port = 0) {
        routing {
            get("/") {
                val code = call.parameters["code"]
                if(code != null) {
                    val session = auth.exchangeCodeForSession(code, false)
                    onSuccess(session)
                    shutdown(call, auth.config.httpCallbackConfig.redirectHtml)
                } else {
                    call.respondText(ContentType.Text.Html, HttpStatusCode.OK) { HTML.landingPage(auth.config.httpCallbackConfig.htmlTitle) }
                }
            }
            get("/callback") {
                Auth.logger.d {
                    "Received request on OAuth callback route"
                }
                val accessToken = call.parameters["access_token"] ?: return@get
                val refreshToken = call.parameters["refresh_token"] ?: return@get
                val expiresIn = call.parameters["expires_in"]?.toLong() ?: return@get
                val tokenType = call.parameters["token_type"] ?: return@get
                val providerToken = call.parameters["provider_token"]
                val providerRefreshToken = call.parameters["provider_refresh_token"]
                val type = call.parameters["type"].orEmpty()
                val user = auth.retrieveUser(accessToken)
                onSuccess(UserSession(accessToken, refreshToken, providerRefreshToken, providerToken, expiresIn, tokenType, user, type))
                Auth.logger.d {
                    "Successfully authenticated user with OAuth"
                }
                shutdown(call, auth.config.httpCallbackConfig.redirectHtml)
            }
        }
    }
    coroutineScope {
        val timeoutScope = launch {
            val port = server.resolvedConnectors().first().port
            Auth.logger.d {
                "Started OAuth callback server on port $port. Opening url in browser..."
            }
            auth.supabaseClient.openExternalUrl(
                url(
                    "http://localhost:$port"
                )
            )
            delay(auth.config.httpCallbackConfig.timeout)
            if(this.isActive) {
                Auth.logger.d {
                    "Timeout reached. Shutting down callback server..."
                }
                server.stop()
            } else {
                Auth.logger.d {
                    "Callback server was shut down manually"
                }
            }
        }
        launch {
            suspendCancellableCoroutine {
                server.environment.monitor.subscribe(ApplicationStopPreparing) { _ ->
                    it.resume(Unit)
                    timeoutScope.cancel()
                }
                server.start()
                it.invokeOnCancellation {
                    server.stop()
                    timeoutScope.cancel()
                }
            }
        }.join()
    }
}

private suspend fun shutdown(call: ApplicationCall, message: String) {
    val application = call.application
    val environment = application.environment

    val latch = CompletableDeferred<Nothing>()
    call.application.launch {
        latch.join()

        environment.monitor.raise(ApplicationStopPreparing, environment)
        if (environment is ApplicationEngineEnvironment) {
            environment.stop()
        } else {
            application.dispose()
        }
    }

    try {
        call.respondText(ContentType.Text.Html, HttpStatusCode.OK) { message }
    } finally {
        latch.cancel()
    }
}

internal object HTML {

    fun landingPage(title: String) = """
            <!DOCTYPE html>
            <html lang="en">
                  <head>
                        <title>$title</title>
                  </head>

                  <body>
                        <script>
                            const pairs = location.hash.substring(1).split("&").map(pair => pair.split("="))
                            const accessToken = pairs.find(pair => pair[0] === "access_token")[1]
                            const refreshToken = pairs.find(pair => pair[0] === "refresh_token")[1]
                            const expiresIn = pairs.find(pair => pair[0] === "expires_in")[1]
                            const tokenType = pairs.find(pair => pair[0] === "token_type")[1]
                            location.href = "/callback?access_token=" + accessToken + "&refresh_token=" + refreshToken + "&expires_in=" + expiresIn + "&token_type=" + tokenType
                       </script>
                 </body>
            </html>
        """.trimIndent()

    fun redirectPage(icon: String, title: String, text: String) = """
        <!DOCTYPE html>
        <html>
            <head>
                <meta charset="utf-8">
                <link rel="icon" href="$icon">
                <title>$title</title>
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
                ">$text</p>
                <script>
                    const newURL = location.href.split("?")[0];
                    window.history.replaceState({}, document.title, newURL);
                </script>
            </body>
        </html>
    """.trimIndent()

}