import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.user.UserSession
import io.github.jan.supabase.putJsonObject
import io.github.jan.supabase.realtime.Presence
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.RealtimeChannel.Companion.CHANNEL_EVENT_PRESENCE_DIFF
import io.github.jan.supabase.realtime.RealtimeChannel.Companion.CHANNEL_EVENT_SYSTEM
import io.github.jan.supabase.realtime.RealtimeJoinPayload
import io.github.jan.supabase.realtime.RealtimeMessage
import io.github.jan.supabase.realtime.RealtimeTopic
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.put
import kotlin.time.Clock
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds

val FLOW_TIMEOUT = 6.seconds

suspend fun Auth.importAuthTokenValid(token: String) {
    importSession(UserSession(token, "", expiresAt = Clock.System.now() + 1.hours, expiresIn = 1.hours.inWholeSeconds, tokenType = ""))
}

suspend fun handleSubscribe(
    incoming: ReceiveChannel<RealtimeMessage>,
    outgoing: SendChannel<RealtimeMessage>,
    channelId: String,
    expectEnabledPresence: Boolean = false
) {
    val payload = Json.decodeFromJsonElement<RealtimeJoinPayload>(incoming.receive().payload)
    assertEquals(expectEnabledPresence, payload.config.presence.enabled)
    outgoing.send(RealtimeMessage(RealtimeTopic.withChannelId(channelId), CHANNEL_EVENT_SYSTEM, buildJsonObject { put("status", "ok") }, ""))
}

suspend fun handleUnsubscribe(
    incoming: ReceiveChannel<RealtimeMessage>,
    outgoing: SendChannel<RealtimeMessage>,
    channelId: String
) {
    val message = incoming.receive()
    assertEquals(RealtimeTopic.withChannelId(channelId), message.topic)
    outgoing.send(RealtimeMessage(RealtimeTopic.withChannelId(channelId), RealtimeChannel.CHANNEL_EVENT_CLOSE, buildJsonObject {  }, ""))
}

suspend fun SendChannel<RealtimeMessage>.sendBroadcast(channelId: String, event: String, message: JsonObject) {
    send(RealtimeMessage(RealtimeTopic.withChannelId(channelId), "broadcast", buildJsonObject {
        put("event", event)
        put("payload", message)
    }, ""))
}

suspend fun SendChannel<RealtimeMessage>.sendPresence(channelId: String, joins: Map<String, Presence>, leaves: Map<String, Presence>) {
    send(RealtimeMessage(RealtimeTopic.withChannelId(channelId), CHANNEL_EVENT_PRESENCE_DIFF, buildJsonObject {
        put("joins", transformPresenceMap(joins))
        put("leaves", transformPresenceMap(leaves))
    }, ""))
}

fun transformPresenceMap(map: Map<String, Presence>): JsonElement {
    return Json.encodeToJsonElement(map.mapValues {
        buildJsonObject {
            put("metas", buildJsonArray {
                add(buildJsonObject {
                    put("phx_ref", it.value.presenceRef)
                    putJsonObject(it.value.state)
                })
            })
        }
    })
}

suspend inline fun <reified T> Flow<T>.waitForValue(value: T) = filter { it == value }.first()