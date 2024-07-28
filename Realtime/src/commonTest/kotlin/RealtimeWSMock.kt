import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.SupabaseClientBuilder
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.logging.LogLevel
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.RealtimeMessage
import io.github.jan.supabase.realtime.websocket.RealtimeWebsocket
import io.github.jan.supabase.realtime.websocket.RealtimeWebsocketFactory
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.hours

class MockWSFactory(
    private val clientChannel: Channel<RealtimeMessage>,
    private val serverChannel: Channel<RealtimeMessage>
) : RealtimeWebsocketFactory {

    override suspend fun create(url: String): RealtimeWebsocket {
        return MockWS(clientChannel, serverChannel)
    }

}

@OptIn(DelicateCoroutinesApi::class)
class MockWS(
    private val sendChannel: SendChannel<RealtimeMessage>,
    private val receiveChannel: ReceiveChannel<RealtimeMessage>
) : RealtimeWebsocket {

    override val hasIncomingMessages: Boolean
        get() = !receiveChannel.isClosedForReceive

    override suspend fun receive(): RealtimeMessage {
        return receiveChannel.receive()
    }

    override suspend fun send(message: RealtimeMessage) {
        sendChannel.send(message)
    }

    override suspend fun blockUntilDisconnect() {
        TODO()
    }

    override fun disconnect() {
        sendChannel.close()
        receiveChannel.cancel()
    }

}

suspend fun createTestClient(
    wsHandler: suspend (incoming: ReceiveChannel<RealtimeMessage>, outgoing: SendChannel<RealtimeMessage>) -> Unit,
    supabaseHandler: suspend (SupabaseClient) -> Unit,
    realtimeConfig: Realtime.Config.() -> Unit = {},
    supabaseConfig: SupabaseClientBuilder.() -> Unit = {},
    mockEngineHandler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData = { respond("") }
) {
    val serverChannel = Channel<RealtimeMessage>()
    val clientChannel = Channel<RealtimeMessage>()
    val supabase = createSupabaseClient("", "") {
        defaultLogLevel = LogLevel.DEBUG
        httpEngine = MockEngine(mockEngineHandler)
        install(Realtime) {
            websocketFactory = MockWSFactory(serverChannel, clientChannel)
            heartbeatInterval = 1.hours
            realtimeConfig()
        }
        supabaseConfig()
    }
    coroutineScope {
        launch {
            supabaseHandler(supabase)
        }
        launch {
            wsHandler(serverChannel, clientChannel)
        }.join()
    }
    supabase.close()
    serverChannel.close()
    serverChannel.cancel()
    clientChannel.close()
    clientChannel.cancel()
}