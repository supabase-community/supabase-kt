import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.minimalSettings
import io.github.jan.supabase.realtime.RealtimeJoinPayload
import io.github.jan.supabase.realtime.RealtimeMessage
import io.github.jan.supabase.realtime.channel
import io.ktor.server.websocket.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class RealtimeChannelTest {

    @Test
    fun testConnectOnSubscribe() {
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

}