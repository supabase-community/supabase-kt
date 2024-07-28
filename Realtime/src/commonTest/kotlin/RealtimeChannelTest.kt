import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.minimalSettings
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.RealtimeChannel.Companion.CHANNEL_EVENT_REPLY
import io.github.jan.supabase.realtime.RealtimeChannel.Companion.CHANNEL_EVENT_SYSTEM
import io.github.jan.supabase.realtime.RealtimeJoinPayload
import io.github.jan.supabase.realtime.RealtimeMessage
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class RealtimeChannelTest {

    @Test
    fun testConnectOnSubscribeDisabled() {
        runTest {
            createTestClient(
                wsHandler = { _, _ ->
                    //Does not matter for this test
                },
                supabaseHandler = {
                    val channel = it.channel("")
                    assertFailsWith<IllegalStateException>() {
                        channel.subscribe()
                    }
                },
                realtimeConfig = {
                    connectOnSubscribe = false
                }
            )
        }
    }

    @Test
    fun testConnectOnSubscribeEnabled() {
        runTest {
            createTestClient(
                wsHandler = { i, _ ->
                    i.receive()
                },
                supabaseHandler = {
                    val channel = it.channel("")
                    channel.subscribe(false)
                    assertEquals(Realtime.Status.CONNECTED, it.realtime.status.value)
                }
            )
        }
    }

    @Test
    fun testChannelStatusWithoutPostgres() {
        val channelId = "channelId"
        runTest {
            createTestClient(
                wsHandler = { i, o ->
                    i.receive()
                    o.send(RealtimeMessage("realtime:$channelId", CHANNEL_EVENT_SYSTEM, buildJsonObject { put("status", "ok") }, ""))
                    i.receive()
                    o.send(RealtimeMessage("realtime:$channelId", CHANNEL_EVENT_REPLY, buildJsonObject { put("status", "ok") }, ""))
                },
                supabaseHandler = {
                    val channel = it.channel("channelId")
                    assertEquals(channel.status.value, RealtimeChannel.Status.UNSUBSCRIBED)
                    assertEquals(it.realtime.status.value, Realtime.Status.DISCONNECTED)
                    channel.subscribe(blockUntilSubscribed = true)
                    assertEquals(channel.status.value, RealtimeChannel.Status.SUBSCRIBED)
                    channel.unsubscribe()
                    assertEquals(channel.status.value, RealtimeChannel.Status.UNSUBSCRIBING)
                    assertEquals(RealtimeChannel.Status.UNSUBSCRIBED, channel.status.waitForValue(RealtimeChannel.Status.UNSUBSCRIBED))
                },
            )
        }
    }

    @Test
    fun testSendingPayloadWithoutJWT() {
        val expectedChannelId = "channelId"
        val expectedIsPrivate = true
        val expectedReceiveOwnBroadcasts = true
        val expectedAcknowledge = true
        val expectedPresenceKey = "presenceKey"
        runTest {
            createTestClient(
                wsHandler = { i, o ->
                    val message = i.receive()
                    val payload = Json.decodeFromJsonElement<RealtimeJoinPayload>(message.payload)
                    assertEquals("realtime:$expectedChannelId", message.topic)
                    assertEquals(expectedIsPrivate, payload.config.isPrivate)
                    assertEquals(expectedReceiveOwnBroadcasts, payload.config.broadcast.receiveOwnBroadcasts)
                    assertEquals(expectedAcknowledge, payload.config.broadcast.acknowledgeBroadcasts)
                    assertEquals(expectedPresenceKey, payload.config.presence.key)
                },
                supabaseHandler = {
                    val channel = it.channel("channelId") {
                        isPrivate = expectedIsPrivate
                        broadcast {
                            receiveOwnBroadcasts = expectedReceiveOwnBroadcasts
                            acknowledgeBroadcasts = expectedAcknowledge
                        }
                        presence {
                            key = expectedPresenceKey
                        }
                    }
                    channel.subscribe()
                }
            )
        }
    }

    @Test
    fun testSendingPayloadWithAuthJWT() {
        val expectedAuthToken = "authToken"
        runTest {
            createTestClient(
                wsHandler = { i, _ ->
                    val message = i.receive()
                    assertEquals(expectedAuthToken, message.payload["access_token"]?.jsonPrimitive?.content)
                },
                supabaseHandler = {
                    it.auth.importAuthTokenValid(expectedAuthToken)
                    val channel = it.channel("channelId")
                    channel.subscribe()
                },
                supabaseConfig = {
                    install(Auth) {
                        minimalSettings()
                    }
                }
            )
        }
    }

    @Test
    fun testSendingPayloadWithCustomJWT() {
        val expectedAuthToken = "authToken"
        runTest {
            createTestClient(
                wsHandler = { i, _ ->
                    val message = i.receive()
                    assertEquals(expectedAuthToken, message.payload["access_token"]?.jsonPrimitive?.content)
                },
                supabaseHandler = {
                    val channel = it.channel("channelId")
                    channel.subscribe()
                },
                realtimeConfig = {
                    jwtToken = expectedAuthToken
                }
            )
        }
    }

    @Test
    fun testSendingBroadcasts() {
        val message = buildJsonObject {
            put("key", "value")
        }
        val event = "event"
        runTest {
            createTestClient(
                wsHandler = { i, o ->
                    handleSubscribe(i, o,"channelId")
                    val rMessage = i.receive()
                    assertEquals("realtime:channelId", rMessage.topic)
                    assertEquals("broadcast", rMessage.event)
                    assertEquals(message, rMessage.payload["payload"]?.jsonObject)
                    assertEquals(event, rMessage.payload["event"]?.jsonPrimitive?.content)
                    assertEquals("broadcast", rMessage.payload["type"]?.jsonPrimitive?.content)
                },
                supabaseHandler = {
                    val channel = it.channel("channelId")
                    channel.subscribe(true)
                    channel.broadcast(event, message)
                }
            )
        }
    }

    @Test
    fun testSendingPresenceUnsubscribed() {
        runTest {
            createTestClient(
                wsHandler = { i, o ->
                },
                supabaseHandler = {
                    val channel = it.channel("channelId")
                    assertFailsWith<IllegalStateException> {
                        channel.track(buildJsonObject {  })
                    }
                }
            )
        }
    }

}