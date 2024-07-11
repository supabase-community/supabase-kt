import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.RealtimeMessage
import io.github.jan.supabase.realtime.realtime
import io.ktor.server.websocket.receiveDeserialized
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.test.Test
import kotlin.test.assertEquals

class RealtimeTest {
    
    @Test
    fun testRealtimeStatus() {
        createTestClient(
            wsHandler = {
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
        createTestClient(
            wsHandler = {
                val message = this.receiveDeserialized<RealtimeMessage>()
                assertEquals(expectedMessage, message)
            },
            supabaseHandler = {
                it.realtime.connect()
                it.realtime.send(expectedMessage)
            }
        )
    }

}