import io.github.jan.supabase.realtime.RealtimeMessage
import io.github.jan.supabase.realtime.realtime
import io.ktor.server.websocket.receiveDeserialized
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.test.Test
import kotlin.test.assertEquals

class RealtimeTest {

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