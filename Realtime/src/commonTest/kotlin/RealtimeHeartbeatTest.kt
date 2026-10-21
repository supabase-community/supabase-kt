import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.RealtimeMessage
import io.github.jan.supabase.realtime.RealtimeProtocolVersion
import io.github.jan.supabase.realtime.broadcast.encodeV2Text
import io.github.jan.supabase.realtime.realtime
import io.github.jan.supabase.realtime.websocket.RealtimeWebsocket
import io.github.jan.supabase.realtime.websocket.RealtimeWebsocketFactory
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.websocket.Frame
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@OptIn(SupabaseInternal::class)
class RealtimeHeartbeatTest {

    private class HeartbeatSocket(private val answerHeartbeats: Boolean) : RealtimeWebsocket {
        private val incoming = Channel<Frame>(Channel.UNLIMITED)
        private var ref = 0
        var heartbeats = 0
            private set
        var disconnected = false
            private set

        override val hasIncomingMessages: Boolean get() = !incoming.isClosedForReceive
        override fun makeRef(): String = (++ref).toString()
        override suspend fun receive(): Frame = incoming.receive()

        override suspend fun send(message: RealtimeMessage, vsn: RealtimeProtocolVersion) {
            if (message.event != "heartbeat") return
            heartbeats++
            if (answerHeartbeats) {
                val reply = message.copy(
                    event = "phx_reply",
                    payload = buildJsonObject {
                        put("status", "ok")
                        putJsonObject("response") { }
                    }
                )
                incoming.send(Frame.Text(reply.encodeV2Text()))
            }
        }

        override suspend fun send(data: ByteArray) = Unit
        override suspend fun blockUntilDisconnect() = Unit

        override fun disconnect() {
            disconnected = true
            incoming.close()
        }

        /** The connection is lost without a close from the client, e.g. while the device sleeps. */
        fun lose() {
            incoming.close(IllegalStateException("Connection lost"))
        }
    }

    @Test
    fun testUnansweredHeartbeatOfALostSocketDoesNotTearDownTheNextSocket() {
        runTest {
            val sockets = mutableListOf<HeartbeatSocket>()
            val client = createSupabaseClient("https://example.supabase.co", "anon-key") {
                httpEngine = MockEngine { respond("") }
                install(Realtime) {
                    coroutineScope = backgroundScope
                    heartbeatInterval = 15.seconds
                    reconnectDelay = 7.seconds
                    disconnectOnNoSubscriptions = false
                    websocketFactory = object : RealtimeWebsocketFactory {
                        // Only the first socket leaves its heartbeat unanswered.
                        override suspend fun create(url: String): RealtimeWebsocket =
                            HeartbeatSocket(answerHeartbeats = sockets.isNotEmpty()).also(sockets::add)
                    }
                }
            }
            client.realtime.connect()

            advanceTimeBy(15.seconds + 100.milliseconds)
            assertEquals(1, sockets.single().heartbeats)
            sockets.single().lose()
            advanceTimeBy(7.seconds + 100.milliseconds)
            assertEquals(2, sockets.size, "The client reconnects after the reconnect delay")

            advanceTimeBy(15.seconds + 100.milliseconds)
            val replacement = sockets.last()
            assertEquals(1, replacement.heartbeats, "The new socket sends its own first heartbeat")
            assertFalse(replacement.disconnected, "The new socket is not closed for the old socket's missing reply")
            assertEquals(2, sockets.size)
            assertEquals(Realtime.Status.CONNECTED, client.realtime.status.value)
        }
    }
}
