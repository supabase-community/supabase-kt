import io.github.jan.supabase.auth.jwt.JWTUtils
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.RealtimeImpl
import io.github.jan.supabase.realtime.RealtimeMessage
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Clock

class RealtimeTest {

    @Test
    fun testRealtimeStatus() {
        runTest {
            createTestClient(
                wsHandler = { _, _ ->
                    //Does not matter for this test
                },
                supabaseHandler = {
                    assertEquals(Realtime.Status.DISCONNECTED, it.realtime.status.value)
                    it.realtime.connect()
                    assertEquals(Realtime.Status.CONNECTED, it.realtime.status.value)
                    it.realtime.disconnect()
                    assertEquals(Realtime.Status.DISCONNECTED, it.realtime.status.value)
                }
            )
        }
    }

    @Test
    fun testExistingChannelShouldBeReturned() {
        runTest {
            createTestClient(
                wsHandler = { _, _ ->
                    //Does not matter for this test
                },
                supabaseHandler = {
                    val channel = it.realtime.channel("channelId")
                    val channel2 = it.realtime.channel("channelId")
                    assertEquals(channel, channel2)
                }
            )
        }
    }

    @Test
    fun testSendingRealtimeMessages() {
        val expectedMessage = RealtimeMessage(
            topic = "realtimeTopic",
            event = "realtimeEvent",
            payload = buildJsonObject {
                put("key", "value")
            },
            ref = "realtimeRef"
        )
        runTest {
            createTestClient(
                wsHandler = { i, _ ->
                    val message = i.receive()
                    assertEquals(expectedMessage, message)
                },
                supabaseHandler = {
                    it.realtime.connect()
                    it.realtime.send(expectedMessage)
                }
            )
        }
    }

    @Test
    fun testSendingExpiredToken() {
        runTest {
            createTestClient(
                wsHandler = { i, _ ->

                },
                supabaseHandler = {
                    it.realtime as RealtimeImpl
                    val expiredToken = generateToken(Clock.System.now().epochSeconds - 10)
                    it.realtime.setAuth(expiredToken)
                    assertNull((it.realtime as RealtimeImpl).accessToken)
                }
            )
        }
    }

    @Test
    fun testSendingValidToken() {
        runTest {
            createTestClient(
                wsHandler = { i, _ ->

                },
                supabaseHandler = {
                    it.realtime as RealtimeImpl
                    val token = generateToken(Clock.System.now().epochSeconds + 10)
                    it.realtime.setAuth(token)
                    assertEquals(token, (it.realtime as RealtimeImpl).accessToken)
                }
            )
        }
    }

    private fun generateToken(exp: Long) = buildString {
        append("${JWTUtils.encodeToBase64Url("{\"alg\":\"HS256\"}")}.")
        append(JWTUtils.encodeToBase64Url(buildJsonObject {
            put("exp", exp)
        }.toString()))
        append(".test")
    }

}