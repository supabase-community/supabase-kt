import app.cash.turbine.test
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.RealtimeMessage
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.seconds

class RealtimeHeartbeatTest {

    private val heartbeatInterval = 15.seconds

    @Test
    fun testChannelMessageWithRefZeroIsNotTakenForHeartbeatReply() {
        val channelId = "channelId"
        runTest {
            createTestClient(
                wsHandler = { i, o ->
                    handleSubscribe(i, o, channelId)
                    // The server closes a join with its join_ref as the ref, and a socket's first join_ref is 0
                    o.send(RealtimeMessage("realtime:$channelId", RealtimeChannel.CHANNEL_EVENT_CLOSE, buildJsonObject { }, "0", "0").toFrame())
                    // Removing the subscribed channel sends a leave
                    assertEquals(RealtimeChannel.CHANNEL_EVENT_LEAVE, i.receive().toMessage().event)
                },
                supabaseHandler = {
                    val channel = it.channel(channelId)
                    channel.subscribe(blockUntilSubscribed = true)
                    channel.status.test(FLOW_TIMEOUT) {
                        while(awaitItem() != RealtimeChannel.Status.UNSUBSCRIBED) Unit
                    }
                    assertNull(it.realtime.subscriptions["realtime:$channelId"])
                },
            )
        }
    }

    @Test
    fun testUnansweredFirstHeartbeatTimesOut() {
        val sockets = ScriptedWebsocketFactory()
        runTest {
            createTestClient(
                wsHandler = { _, _ -> },
                supabaseHandler = {
                    it.realtime.connect()
                    advanceTimeBy(heartbeatInterval + 1.seconds)
                    assertEquals("0", sockets.created[0].nextHeartbeat().ref)
                    advanceTimeBy(heartbeatInterval)
                    assertEquals(Realtime.Status.DISCONNECTED, it.realtime.status.value)
                },
                realtimeConfig = {
                    coroutineScope = backgroundScope
                    heartbeatInterval = this@RealtimeHeartbeatTest.heartbeatInterval
                    websocketFactory = sockets
                },
            )
        }
    }

    @Test
    fun testHeartbeatPendingAtDisconnectDoesNotTimeOutNextSocket() {
        val sockets = ScriptedWebsocketFactory()
        runTest {
            createTestClient(
                wsHandler = { _, _ -> },
                supabaseHandler = {
                    it.realtime.connect()
                    val first = sockets.created[0]
                    advanceTimeBy(heartbeatInterval + 1.seconds)
                    first.reply(first.nextHeartbeat())
                    advanceTimeBy(heartbeatInterval)
                    // Left unanswered
                    assertEquals("1", first.nextHeartbeat().ref)
                    it.realtime.disconnect()
                    it.realtime.connect()
                    advanceTimeBy(heartbeatInterval + 1.seconds)
                    assertEquals(Realtime.Status.CONNECTED, it.realtime.status.value)
                    assertEquals("heartbeat", sockets.created[1].nextHeartbeat().event)
                },
                realtimeConfig = {
                    coroutineScope = backgroundScope
                    heartbeatInterval = this@RealtimeHeartbeatTest.heartbeatInterval
                    websocketFactory = sockets
                },
            )
        }
    }

    private suspend fun ScriptedWebsocket.nextHeartbeat(): RealtimeMessage {
        val message = nextSent()
        assertEquals("heartbeat", message.event)
        return message
    }

    private suspend fun ScriptedWebsocket.reply(heartbeat: RealtimeMessage) = push(
        RealtimeMessage("phoenix", RealtimeChannel.CHANNEL_EVENT_REPLY, buildJsonObject {
            put("status", "ok")
            put("response", buildJsonObject { })
        }, heartbeat.ref)
    )

}
