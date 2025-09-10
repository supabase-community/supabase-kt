import io.github.jan.supabase.SupabaseSerializer
import io.github.jan.supabase.realtime.CallbackManagerImpl
import io.github.jan.supabase.realtime.HasRecord
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.PostgresJoinConfig
import io.github.jan.supabase.realtime.Presence
import io.github.jan.supabase.serializer.KotlinXSerializer
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Clock

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

    @Test
    fun testPresenceCallbacks() {
        val cm = CallbackManagerImpl()
        val expectedJoins = mapOf("join" to Presence("join", buildJsonObject {
            put("key", "value")
        }))
        val expectedLeaves = mapOf("leave" to Presence("leave", buildJsonObject {
            put("key", "value")
        }))
        var called = false
        val id = cm.addPresenceCallback {
            assertEquals(expectedJoins, it.joins)
            assertEquals(expectedLeaves, it.leaves)
            called = true
        }
        assertTrue { cm.hasPresenceCallback() }
        cm.triggerPresenceDiff(expectedJoins, expectedLeaves)
        assertTrue { called }
        cm.removeCallbackById(id)
        called = false
        assertFalse { cm.hasPresenceCallback() }
        cm.triggerPresenceDiff(expectedJoins, expectedLeaves)
        assertFalse { called }
    }

    @Test
    fun testPostgresCallbacks() {
        val events = listOf("INSERT", "UPDATE", "DELETE", "*")
        for(event in events) {
            val cm = CallbackManagerImpl()
            val joinConfig = PostgresJoinConfig("public", "table", event = event)
            val expectedRecord = buildJsonObject {
                put("key", "value")
            }
            val expectedOldRecord = buildJsonObject {
                put("key", "oldValue")
            }
            var called = false
            val id = cm.addPostgresCallback(joinConfig) {
                assertTrue { it.shouldBeCalled(event) }
                if(it is HasRecord) {
                    assertEquals(expectedRecord, it.record)
                }
                if(it is PostgresAction.Update) {
                    assertEquals(expectedOldRecord, it.oldRecord)
                }
                called = true
            }
            cm.setServerChanges(listOf(joinConfig))
            cm.triggerPostgresChange(listOf(id.id), actionFromEvent(event, expectedRecord, expectedOldRecord))
            assertTrue { called }
            called = false
            if(event != "*") {
                cm.triggerPostgresChange(listOf(2), actionFromEvent(events.filter { it != event && it != "*" }.random(), expectedRecord, expectedOldRecord))
                assertFalse { called }
            }
            cm.removeCallbackById(id)
            cm.triggerPostgresChange(listOf(id.id), actionFromEvent(event, expectedRecord, expectedOldRecord))
            assertFalse { called }
        }
    }

    private fun PostgresAction.shouldBeCalled(event: String): Boolean {
        return when(this) {
            is PostgresAction.Insert -> event == "INSERT"
            is PostgresAction.Update -> event == "UPDATE"
            is PostgresAction.Delete -> event == "DELETE"
            is PostgresAction.Select -> event == "SELECT"
        } || event == "*"
    }

    private fun actionFromEvent(event: String, record: JsonObject, oldRecord: JsonObject, serializer: SupabaseSerializer = KotlinXSerializer()): PostgresAction {
        return when(event) {
            "INSERT" -> PostgresAction.Insert(record, listOf(), Clock.System.now(), serializer)
            "UPDATE" -> PostgresAction.Update(record, oldRecord, listOf(), Clock.System.now(), serializer)
            "DELETE" -> PostgresAction.Delete(record, listOf(), Clock.System.now(), serializer)
            "SELECT" -> PostgresAction.Select(record, listOf(), Clock.System.now(), serializer)
            "*" -> PostgresAction.Insert(record, listOf(), Clock.System.now(), serializer)
            else -> error("Unknown event $event")
        }
    }

}