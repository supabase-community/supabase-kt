import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.RealtimeMessage
import io.github.jan.supabase.realtime.RealtimeSystemPayload
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.fail
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

// Channel errors and rejoins, as in realtime-js (Phoenix's client): a `system` error is reported and the channel stays
// joined; a channel error or a rejected join rejoins by sending the join again.
class RealtimeChannelRejoinTest {

    private val channelId = "channelId"
    private val topic = "realtime:$channelId"

    @Test
    fun testRefusedPostgresChangesReachSystemFlowWithoutRejoining() = runRejoinTest { client, sockets ->
        val channel = client.channel(channelId)
        channel.postgresChangeFlow<PostgresAction>("public") { table = "table" }
        val system = Channel<RealtimeSystemPayload>(Channel.UNLIMITED)
        backgroundScope.launch { channel.systemFlow().collect { system.send(it) } }
        val (socket, _) = subscribe(channel, sockets)
        socket.push(refused())
        // The collector may or may not have seen the join's "ok"
        val error = within {
            var payload = system.receive()
            while(payload.status == "ok") payload = system.receive()
            payload
        }
        assertEquals("error", error.status)
        assertEquals("postgres_changes", error.extension)
        assertNothingSent(socket)
        assertEquals(RealtimeChannel.Status.SUBSCRIBED, channel.status.value)
    }

    @Test
    fun testRejectedJoinIsRetriedWithoutLeaving() = runRejoinTest { client, sockets ->
        val channel = client.channel(channelId)
        channel.subscribe()
        val socket = sockets.created.last()
        socket.push(rejected(nextSent(socket, RealtimeChannel.CHANNEL_EVENT_JOIN)))
        nextSent(socket, RealtimeChannel.CHANNEL_EVENT_JOIN)
        socket.push(subscribed())
        eventually { channel.status.value == RealtimeChannel.Status.SUBSCRIBED }
    }

    @Test
    fun testRejectedJoinsGiveUpAfterMaxAttempts() = runRejoinTest({ maxAttempts = 3 }) { client, sockets ->
        val channel = client.channel(channelId)
        channel.subscribe()
        val socket = sockets.created.last()
        repeat(3) {
            socket.push(rejected(nextSent(socket, RealtimeChannel.CHANNEL_EVENT_JOIN)))
        }
        eventually { channel.status.value == RealtimeChannel.Status.UNSUBSCRIBED }
        assertNothingSent(socket)
    }

    @Test
    fun testChannelErrorRejoinsWithoutLeaving() = runRejoinTest { client, sockets ->
        val channel = client.channel(channelId)
        val (socket, joinRef) = subscribe(channel, sockets)
        socket.push(channelError(joinRef))
        nextSent(socket, RealtimeChannel.CHANNEL_EVENT_JOIN)
        socket.push(subscribed())
        eventually { channel.status.value == RealtimeChannel.Status.SUBSCRIBED }
    }

    @Test
    fun testCloseForReplacedJoinIsIgnored() = runRejoinTest { client, sockets ->
        val channel = client.channel(channelId)
        val (socket, replaced) = subscribe(channel, sockets)
        socket.push(channelError(replaced))
        nextSent(socket, RealtimeChannel.CHANNEL_EVENT_JOIN)
        // The server closes the join the new one replaced
        socket.push(close(replaced))
        socket.push(subscribed())
        eventually { channel.status.value == RealtimeChannel.Status.SUBSCRIBED && client.realtime.subscriptions.containsKey(topic) }
        assertEquals(1, sockets.created.size)
    }

    @Test
    fun testSuccessfulJoinResetsAttempts() = runRejoinTest({ maxAttempts = 2 }) { client, sockets ->
        val channel = client.channel(channelId)
        val (socket, firstJoinRef) = subscribe(channel, sockets)
        socket.push(channelError(firstJoinRef))
        val secondJoinRef = nextSent(socket, RealtimeChannel.CHANNEL_EVENT_JOIN).joinRef
        socket.push(subscribed())
        eventually { channel.status.value == RealtimeChannel.Status.SUBSCRIBED }
        // Counting starts over after the join succeeded, so this is retried too
        socket.push(channelError(secondJoinRef))
        nextSent(socket, RealtimeChannel.CHANNEL_EVENT_JOIN)
    }

    private fun runRejoinTest(
        config: Realtime.Config.() -> Unit = {},
        test: suspend TestScope.(SupabaseClient, ScriptedWebsocketFactory) -> Unit,
    ) {
        val sockets = ScriptedWebsocketFactory()
        runTest {
            createTestClient(
                wsHandler = { _, _ -> },
                supabaseHandler = { test(it, sockets) },
                realtimeConfig = {
                    websocketFactory = sockets
                    disconnectOnNoSubscriptions = true
                    disconnectOnEmptyChannelsAfter = Duration.ZERO
                    rejoinDelay = 10.milliseconds
                    config()
                },
            )
        }
    }

    // Subscribes on a new socket and returns it with the join's ref.
    private suspend fun subscribe(channel: RealtimeChannel, sockets: ScriptedWebsocketFactory): Pair<ScriptedWebsocket, String?> {
        channel.subscribe()
        val socket = sockets.created.last()
        val joinRef = nextSent(socket, RealtimeChannel.CHANNEL_EVENT_JOIN).joinRef
        socket.push(subscribed())
        eventually { channel.status.value == RealtimeChannel.Status.SUBSCRIBED }
        return socket to joinRef
    }

    private suspend fun nextSent(socket: ScriptedWebsocket, event: String): RealtimeMessage {
        val message = within { socket.nextSent() }
        assertEquals(event, message.event)
        return message
    }

    private suspend fun assertNothingSent(socket: ScriptedWebsocket) {
        val sent = withContext(Dispatchers.Default) {
            withTimeoutOrNull(300.milliseconds) { socket.nextSent() }
        }
        assertNull(sent, "Unexpected ${sent?.event}")
    }

    // The library runs on its own dispatcher here, so waits are in real time. withTimeoutOrNull, not withTimeout:
    // its TimeoutCancellationException would only cancel the coroutine createTestClient runs the test in, which passes.
    private suspend fun <T> within(block: suspend () -> T): T = withContext(Dispatchers.Default) {
        withTimeoutOrNull(FLOW_TIMEOUT) { block() } ?: fail("Nothing within $FLOW_TIMEOUT")
    }

    private suspend fun eventually(condition: () -> Boolean) = within {
        while(!condition()) delay(10.milliseconds)
    }

    private fun subscribed() = system("ok", "system", "Subscribed")

    private fun refused() = system(
        "error",
        "postgres_changes",
        "Unable to subscribe to changes with given parameters. Please check Realtime is enabled for the given connect parameters: [event: *, schema: public, table: table, filters: [], select: nil]",
    )

    private fun system(status: String, extension: String, message: String) =
        RealtimeMessage(topic, RealtimeChannel.CHANNEL_EVENT_SYSTEM, buildJsonObject {
            put("status", status)
            put("extension", extension)
            put("channel", channelId)
            put("message", message)
        }, null)

    private fun rejected(join: RealtimeMessage) = RealtimeMessage(topic, RealtimeChannel.CHANNEL_EVENT_REPLY, buildJsonObject {
        put("status", "error")
        put("response", buildJsonObject { put("reason", "RealtimeDisabledForConfiguration: Unable to subscribe to changes") })
    }, join.ref, join.joinRef)

    // No ref on these: the server sends the join_ref, but "0" would be taken for a heartbeat reply
    private fun channelError(joinRef: String?) = RealtimeMessage(topic, RealtimeChannel.CHANNEL_EVENT_ERROR, buildJsonObject { }, null, joinRef)

    private fun close(joinRef: String?) = RealtimeMessage(topic, RealtimeChannel.CHANNEL_EVENT_CLOSE, buildJsonObject { }, null, joinRef)

}
