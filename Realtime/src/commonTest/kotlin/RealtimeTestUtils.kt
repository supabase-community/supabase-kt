import io.github.jan.supabase.realtime.RealtimeChannel.Companion.CHANNEL_EVENT_SYSTEM
import io.github.jan.supabase.realtime.RealtimeMessage
import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.server.websocket.sendSerialized
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

suspend fun DefaultWebSocketServerSession.handleSubscribe(channelId: String) {
    incoming.receive()
    sendSerialized(RealtimeMessage("realtime:$channelId", CHANNEL_EVENT_SYSTEM, buildJsonObject { put("status", "ok") }, ""))
}