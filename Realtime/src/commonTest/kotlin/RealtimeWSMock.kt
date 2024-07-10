import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.SupabaseClientBuilder
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.logging.LogLevel
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.supabaseJson
import io.ktor.client.plugins.websocket.*
import io.ktor.serialization.kotlinx.*
import io.ktor.server.testing.*
import io.ktor.server.websocket.*
import io.ktor.server.websocket.WebSockets

fun ApplicationTestBuilder.configureServer(
    handler: suspend DefaultWebSocketServerSession.() -> Unit
) {
    install(WebSockets) {
        contentConverter = KotlinxWebsocketSerializationConverter(supabaseJson)
    }
    routing {
        webSocket("/", handler = handler)
    }
}

fun createTestClient(
    wsHandler: suspend DefaultWebSocketServerSession.() -> Unit,
    supabaseHandler: suspend (SupabaseClient) -> Unit,
    realtimeConfig: Realtime.Config.() -> Unit = {},
    supabaseConfig: SupabaseClientBuilder.() -> Unit = {}
) {
    testApplication {
        configureServer(wsHandler)
        val client = createClient {
            install(io.ktor.client.plugins.websocket.WebSockets) {
                contentConverter = KotlinxWebsocketSerializationConverter(supabaseJson)
            }
        }
        client.webSocket("/") {
            val supabase = createSupabaseClient("", "") {
                defaultLogLevel = LogLevel.DEBUG
                install(Realtime) {
                    websocketSessionProvider = {
                        this@webSocket
                    }
                    realtimeConfig()
                }
                supabaseConfig()
            }
            supabaseHandler(supabase)
            supabase.close()
        }
    }
}