import io.github.jan.supabase.realtime.RealtimeMessage
import io.github.jan.supabase.realtime.RealtimeProtocolVersion
import io.github.jan.supabase.realtime.websocket.RealtimeWebsocket
import io.github.jan.supabase.realtime.websocket.RealtimeWebsocketFactory
import io.ktor.websocket.Frame
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.channels.Channel

/**
 * A new [ScriptedWebsocket] per connection, so a test can disconnect and reconnect.
 */
class ScriptedWebsocketFactory : RealtimeWebsocketFactory {
    val created = mutableListOf<ScriptedWebsocket>()

    override suspend fun create(url: String): RealtimeWebsocket = ScriptedWebsocket().also { created += it }
}

/**
 * A socket the test plays the server for: it reads what the client sent with [nextSent] and sends with [push].
 */
class ScriptedWebsocket : RealtimeWebsocket {
    private val sent = Channel<RealtimeMessage>(Channel.UNLIMITED)
    private val incoming = Channel<Frame>(Channel.UNLIMITED)
    private var ref = 0

    override val hasIncomingMessages: Boolean
        get() = !incoming.isClosedForReceive

    override suspend fun receive(): Frame = incoming.receive()

    override suspend fun send(message: RealtimeMessage, vsn: RealtimeProtocolVersion) {
        sent.send(message)
    }

    override suspend fun send(data: ByteArray) = Unit

    override fun makeRef(): String = (ref++).toString()

    override suspend fun blockUntilDisconnect() = awaitCancellation()

    override fun disconnect() {
        incoming.close()
    }

    suspend fun nextSent(): RealtimeMessage = sent.receive()

    suspend fun push(message: RealtimeMessage) = incoming.send(message.toFrame())
}
