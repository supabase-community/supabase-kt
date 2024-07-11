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
import io.ktor.server.websocket.receiveDeserialized
import io.ktor.server.websocket.sendSerialized
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
        createTestClient(
            wsHandler = {
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

    @Test
    fun testConnectOnSubscribeEnabled() {
        createTestClient(
            wsHandler = {
                incoming.receive()
            },
            supabaseHandler = {
                val channel = it.channel("")
                channel.subscribe(false)
                assertEquals(Realtime.Status.CONNECTED, it.realtime.status.value)
            }
        )
    }

    @Test
    fun testChannelStatusWithoutPostgres() {
        val channelId = "channelId"
        createTestClient(
            wsHandler = {
                incoming.receive()
                sendSerialized(RealtimeMessage("realtime:$channelId", CHANNEL_EVENT_SYSTEM, buildJsonObject { put("status", "ok") }, ""))
                incoming.receive()
                sendSerialized(RealtimeMessage("realtime:$channelId", CHANNEL_EVENT_REPLY, buildJsonObject { put("status", "ok") }, ""))
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

    @Test
    fun testSendingPayloadWithoutJWT() {
        val expectedChannelId = "channelId"
        val expectedIsPrivate = true
        val expectedReceiveOwnBroadcasts = true
        val expectedAcknowledge = true
        val expectedPresenceKey = "presenceKey"
        createTestClient(
            wsHandler = {
                val message = this.receiveDeserialized<RealtimeMessage>()
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

    @Test
    fun testSendingPayloadWithAuthJWT() {
        val expectedAuthToken = "authToken"
        createTestClient(
            wsHandler = {
                val message = this.receiveDeserialized<RealtimeMessage>()
                assertEquals(expectedAuthToken, message.payload["access_token"]?.jsonPrimitive?.content)
            },
            supabaseHandler = {
                it.auth.importAuthToken(expectedAuthToken)
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

    @Test
    fun testSendingPayloadWithCustomJWT() {
        val expectedAuthToken = "authToken"
        createTestClient(
            wsHandler = {
                val message = this.receiveDeserialized<RealtimeMessage>()
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

    @Test
    fun testSendingBroadcasts() {
        val message = buildJsonObject {
            put("key", "value")
        }
        val event = "event"
        createTestClient(
            wsHandler = {
                handleSubscribe("channelId")
                val rMessage = this.receiveDeserialized<RealtimeMessage>()
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

    @Test
    fun testSendingPresenceUnsubscribed() {
        createTestClient(
            wsHandler = {
                handleSubscribe("channelId")
            },
            supabaseHandler = {
                val channel = it.channel("channelId")
                channel.subscribe(true)
                assertFailsWith<IllegalStateException> {
                    channel.track(buildJsonObject {  })
                }
            }
        )
    }

}