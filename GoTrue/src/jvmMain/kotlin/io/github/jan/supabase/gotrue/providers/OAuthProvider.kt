package io.github.jan.supabase.gotrue.providers

import io.github.aakira.napier.Napier
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.gotrue.GoTrueImpl
import io.github.jan.supabase.gotrue.gotrue
import io.github.jan.supabase.gotrue.user.UserSession
import io.javalin.Javalin
import io.javalin.http.ContentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.awt.Desktop
import java.net.URI

actual abstract class OAuthProvider : AuthProvider<ExternalAuthConfig, Unit> {

    actual abstract val name: String

    actual override suspend fun login(
        supabaseClient: SupabaseClient,
        onSuccess: suspend (UserSession) -> Unit,
        redirectUrl: String?,
        config: (ExternalAuthConfig.() -> Unit)?
    ) {
        withContext(Dispatchers.IO) {
            launch {
                createServer(supabaseClient.gotrue, redirectUrl, onSuccess)
            }
        }
    }

    actual override suspend fun signUp(
        supabaseClient: SupabaseClient,
        onSuccess: suspend (UserSession) -> Unit,
        redirectUrl: String?,
        config: (ExternalAuthConfig.() -> Unit)?
    ) = login(supabaseClient, onSuccess, redirectUrl, config = config)

    private suspend fun createServer(
        gotrue: GoTrue,
        redirectUrl: String?,
        onSuccess: suspend (UserSession) -> Unit
    ) {
        if(redirectUrl != null) {
            withContext(Dispatchers.IO) {
                Desktop.getDesktop()
                    .browse(URI(gotrue.resolveUrl("authorize?provider=$name&redirect_to=$redirectUrl")))
            }
            return
        }
        val server = Javalin.create()
            .get("/") { ctx ->
                ctx.html(landingPage(gotrue.config.htmlTitle))
            }
        server.get("/callback") { ctx ->
            Napier.d {
                "Received callback on oauth callback"
            }
            val accessToken = ctx.queryParam("access_token") ?: return@get
            val refreshToken = ctx.queryParam("refresh_token") ?: return@get
            val expiresIn = ctx.queryParam("expires_in")?.toLong() ?: return@get
            val tokenType = ctx.queryParam("token_type") ?: return@get
            val type = ctx.queryParam("type") ?: ""
            (gotrue as GoTrueImpl).authScope.launch {
                val user = gotrue.getUser(accessToken)
                onSuccess(UserSession(accessToken, refreshToken, expiresIn, tokenType, user, type))
            }
            Napier.d {
                "Successfully received callback on oauth callback"
            }
            ctx.html(redirectPage(gotrue.config.htmlIconUrl, gotrue.config.htmlTitle, gotrue.config.htmlText))
            gotrue.authScope.launch(Dispatchers.IO) {
                delay(1000)
                server.stop()
            }
        }
        withContext(Dispatchers.IO) {
            server.start(gotrue.config.httpPort)
            Desktop.getDesktop()
                .browse(URI(gotrue.resolveUrl("authorize?provider=$name&redirect_to=http://localhost:${server.port()}")))
            delay(gotrue.config.timeout)
            server.stop()
        }
    }

    actual companion object {

        private fun landingPage(title: String) = """
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
            </body>
        </html>
    """.trimIndent()

    }

}