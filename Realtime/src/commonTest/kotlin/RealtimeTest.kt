import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.logging.LogLevel
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.realtime
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.server.testing.testApplication
import kotlin.test.Test

class RealtimeTest {

    @Test
    fun test() {
        testApplication {
            val client = createClient {
                install(WebSockets)
            }
            client.webSocket("/") {
                val supabase = createSupabaseClient("", "") {
                    defaultLogLevel = LogLevel.DEBUG
                    install(Realtime) {
                        websocketSessionProvider = {
                            this@webSocket
                        }
                    }
                }
                supabase.realtime.connect()
                //send(Json.encodeToString(RealtimeMessage("topic", "event", buildJsonObject {  }, null)))
            }
        }
    }

}