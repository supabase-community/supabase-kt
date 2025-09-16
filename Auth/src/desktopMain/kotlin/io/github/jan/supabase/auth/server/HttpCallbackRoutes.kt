package io.github.jan.supabase.auth.server

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.handledUrlParameterError
import io.github.jan.supabase.auth.user.UserSession
import io.github.jan.supabase.logging.d
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respondText
import io.ktor.server.routing.Routing
import io.ktor.server.routing.RoutingContext
import io.ktor.server.routing.get

internal fun Routing.configureRoutes(
    auth: Auth,
    onSuccess: suspend (UserSession) -> Unit,
) {
    get("/") {
        val code = call.parameters["code"]
        if (auth.handledUrlParameterError { call.parameters[it] }) {
            errorResponse()
        } else if(code != null) {
            val session = auth.exchangeCodeForSession(code, false)
            onSuccess(session)
            Auth.logger.d {
                "Successfully authenticated user with OAuth using the PKCE flow"
            }
            shutdown(call, auth.config.httpCallbackConfig.redirectHtml)
        } else {
            call.respondText(ContentType.Text.Html, HttpStatusCode.OK) { HttpCallbackHtml.landingPage(auth.config.httpCallbackConfig.htmlTitle) }
        }
    }
    get("/callback") {
        Auth.logger.d {
            "Received request on OAuth callback route"
        }
        if (auth.handledUrlParameterError { call.parameters[it] }) {
            errorResponse()
            return@get
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
            "Successfully authenticated user with OAuth using the implicit flow"
        }
        shutdown(call, auth.config.httpCallbackConfig.redirectHtml)
    }
}

private suspend fun RoutingContext.errorResponse() {
    Auth.logger.d {
        "Error in OAuth callback"
    }
    call.respondText(
        text = "Error",
        contentType = ContentType.Text.Html,
        status = HttpStatusCode.BadRequest
    )
}