import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.SupabaseClientBuilder
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.logging.LogLevel
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.supabaseJson
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.webSocket

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