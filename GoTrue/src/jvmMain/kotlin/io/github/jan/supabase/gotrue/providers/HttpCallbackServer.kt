package io.github.jan.supabase.gotrue.providers

import co.touchlab.kermit.Logger
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.AuthImpl
import io.github.jan.supabase.gotrue.user.UserSession
import io.javalin.Javalin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.awt.Desktop
import java.net.URI

private const val HTTP_SERVER_STOP_DELAY = 1000L

@OptIn(SupabaseExperimental::class)
internal suspend fun createServer(
    url: suspend (redirect: String) -> String,
    gotrue: Auth,
    onSuccess: suspend (UserSession) -> Unit
) {
    val server = Javalin.create()
        .get("/") { ctx ->
            if(ctx.queryParam("code") != null) {
                val code = ctx.queryParam("code") ?: return@get
                (gotrue as AuthImpl).authScope.launch {
                    val session = gotrue.exchangeCodeForSession(code, false)
                    onSuccess(session)
                }
                ctx.html(HTML.redirectPage(gotrue.config.htmlIconUrl, gotrue.config.htmlTitle, gotrue.config.htmlText))
            } else {
                ctx.html(HTML.landingPage(gotrue.config.htmlTitle))
            }
        }
    server.get("/callback") { ctx ->
        Logger.d("Auth") {
            "Received callback on oauth callback"
        }
        val accessToken = ctx.queryParam("access_token") ?: return@get
        val refreshToken = ctx.queryParam("refresh_token") ?: return@get
        val expiresIn = ctx.queryParam("expires_in")?.toLong() ?: return@get
        val tokenType = ctx.queryParam("token_type") ?: return@get
        val providerToken = ctx.queryParam("provider_token")
        val providerRefreshToken = ctx.queryParam("provider_refresh_token")
        val type = ctx.queryParam("type").orEmpty()
        (gotrue as AuthImpl).authScope.launch {
            val user = gotrue.retrieveUser(accessToken)
            onSuccess(UserSession(accessToken, refreshToken, providerRefreshToken, providerToken, expiresIn, tokenType, user, type))
        }
        Logger.d("Auth") {
            "Successfully received http callback"
        }
        ctx.html(HTML.redirectPage(gotrue.config.htmlIconUrl, gotrue.config.htmlTitle, gotrue.config.htmlText))
        gotrue.authScope.launch(Dispatchers.IO) {
            delay(HTTP_SERVER_STOP_DELAY)
            server.stop()
        }
    }
    withContext(Dispatchers.IO) {
        server.start(gotrue.config.httpPort)
        Desktop.getDesktop()
            .browse(URI(url("http://localhost:${server.port()}")))
        delay(gotrue.config.timeout)
        server.stop()
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