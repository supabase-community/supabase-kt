import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.RealtimeMessage
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.test.Test
import kotlin.test.assertEquals

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

}