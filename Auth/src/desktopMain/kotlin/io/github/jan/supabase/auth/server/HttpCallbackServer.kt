package io.github.jan.supabase.auth.server

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.AuthImpl
import io.github.jan.supabase.auth.openExternalUrl
import io.github.jan.supabase.auth.user.UserSession
import io.github.jan.supabase.logging.d
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.ApplicationStopPreparing
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.response.respondText
import io.ktor.server.routing.routing
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

internal suspend fun createServer(
    url: suspend (redirect: String) -> String,
    auth: Auth,
    onSuccess: suspend (UserSession) -> Unit
) {
    auth as AuthImpl
    Auth.logger.d { "Creating OAuth callback server" }
    val server = embeddedServer(CIO, port = 0) {
        routing {
            configureRoutes(
                auth,
                onSuccess
            )
        }
    }
    coroutineScope {
        val timeoutScope = launch {
            val port = server.engine.resolvedConnectors().first().port
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
        suspendCancellableCoroutine {
            server.monitor.subscribe(ApplicationStopPreparing) { _ ->
                it.resume(Unit)
                timeoutScope.cancel()
            }
            server.start()
            it.invokeOnCancellation { _ ->
                server.stop()
            }
        }
    }
}

internal suspend fun shutdown(call: ApplicationCall, message: String) {
    val application = call.application
    val environment = application.environment

    val latch = CompletableDeferred<Nothing>()
    call.application.launch {
        application.monitor.raise(ApplicationStopPreparing, environment)
        latch.join()
        application.dispose()
    }

    try {
        call.respondText(ContentType.Text.Html, HttpStatusCode.OK) { message }
    } finally {
        latch.cancel()
    }
}

