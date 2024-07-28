import io.github.jan.supabase.realtime.CallbackManagerImpl
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CallbackManagerTest {

    @Test
    fun testBroadcastCallbacks() {
        val cm = CallbackManagerImpl()
        val expectedEvent = "event"
        val expectedPayload = buildJsonObject {
            put("key", "value")
        }
        var called = false
        val id = cm.addBroadcastCallback(expectedEvent) {
            assertEquals(expectedPayload, it)
            called = true
        }
        cm.triggerBroadcast(expectedEvent, expectedPayload)
        assertTrue { called }
        cm.removeCallbackById(id)
        called = false
        cm.triggerBroadcast(expectedEvent, expectedPayload)
        assertFalse { called }
    }

}