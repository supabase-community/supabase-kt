import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.RealtimeMessage
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.test.Test
import kotlin.test.assertEquals

class RealtimeMessageEventTypeTest {

    @Test
    fun testSystemType() {
        val message = RealtimeMessage(
            topic = "",
            event = RealtimeChannel.CHANNEL_EVENT_SYSTEM,
            payload = buildJsonObject {
                put("status", "ok")
            },
            ref = ""
        )
        assertEquals(RealtimeMessage.EventType.SYSTEM, message.eventType)
    }

    @Test
    fun testSystemReplyType() {
        val message = RealtimeMessage(
            topic = "",
            event = RealtimeChannel.CHANNEL_EVENT_REPLY,
            payload = buildJsonObject {
                put("status", "ok")
            },
            ref = ""
        )
        assertEquals(RealtimeMessage.EventType.SYSTEM_REPLY, message.eventType)
    }

    @Test
    fun testPostgresServerChangesType() {
        val message = RealtimeMessage(
            topic = "",
            event = RealtimeChannel.CHANNEL_EVENT_REPLY,
            payload = buildJsonObject {
                put("response", buildJsonObject {
                    put(RealtimeChannel.CHANNEL_EVENT_POSTGRES_CHANGES, "")
                })
            },
            ref = ""
        )
        assertEquals(RealtimeMessage.EventType.POSTGRES_SERVER_CHANGES, message.eventType)
    }

    @Test
    fun testPostgresChangesType() {
        val message = RealtimeMessage(
            topic = "",
            event = RealtimeChannel.CHANNEL_EVENT_POSTGRES_CHANGES,
            payload = buildJsonObject {
                put("key", "value")
            },
            ref = ""
        )
        assertEquals(RealtimeMessage.EventType.POSTGRES_CHANGES, message.eventType)
    }

    @Test
    fun testBroadcastType() {
        val message = RealtimeMessage(
            topic = "",
            event = RealtimeChannel.CHANNEL_EVENT_BROADCAST,
            payload = buildJsonObject {
                put("key", "value")
            },
            ref = ""
        )
        assertEquals(RealtimeMessage.EventType.BROADCAST, message.eventType)
    }

    @Test
    fun testCloseType() {
        val message = RealtimeMessage(
            topic = "",
            event = RealtimeChannel.CHANNEL_EVENT_CLOSE,
            payload = buildJsonObject {
                put("key", "value")
            },
            ref = ""
        )
        assertEquals(RealtimeMessage.EventType.CLOSE, message.eventType)
    }

    @Test
    fun testErrorType() {
        val message = RealtimeMessage(
            topic = "",
            event = RealtimeChannel.CHANNEL_EVENT_ERROR,
            payload = buildJsonObject {
                put("key", "value")
            },
            ref = ""
        )
        assertEquals(RealtimeMessage.EventType.ERROR, message.eventType)
    }

    @Test
    fun testPresenceDiffType() {
        val message = RealtimeMessage(
            topic = "",
            event = RealtimeChannel.CHANNEL_EVENT_PRESENCE_DIFF,
            payload = buildJsonObject {
                put("key", "value")
            },
            ref = ""
        )
        assertEquals(RealtimeMessage.EventType.PRESENCE_DIFF, message.eventType)
    }

    @Test
    fun testPresenceStateType() {
        val message = RealtimeMessage(
            topic = "",
            event = RealtimeChannel.CHANNEL_EVENT_PRESENCE_STATE,
            payload = buildJsonObject {
                put("key", "value")
            },
            ref = ""
        )
        assertEquals(RealtimeMessage.EventType.PRESENCE_STATE, message.eventType)
    }

    @Test
    fun testTokenExpiredType() {
        val message = RealtimeMessage(
            topic = "",
            event = RealtimeChannel.CHANNEL_EVENT_SYSTEM,
            payload = buildJsonObject {
                put("message", "access token has expired")
            },
            ref = ""
        )
        assertEquals(RealtimeMessage.EventType.TOKEN_EXPIRED, message.eventType)
    }

}