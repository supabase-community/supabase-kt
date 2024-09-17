package io.github.jan.supabase.realtime.event

import io.github.jan.supabase.logging.d
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.RealtimeMessage
import kotlinx.serialization.json.jsonPrimitive

/**
 * Event that handles the system event
 */
data object RSystemEvent : RealtimeEvent {

    override suspend fun handle(channel: RealtimeChannel, message: RealtimeMessage) {
        Realtime.logger.d { "Subscribed to channel ${message.topic}" }
        channel.updateStatus(RealtimeChannel.Status.SUBSCRIBED)
    }

    override fun appliesTo(message: RealtimeMessage): Boolean {
        return  message.event == RealtimeChannel.CHANNEL_EVENT_SYSTEM && message.payload["status"]?.jsonPrimitive?.content == "ok"
    }

}
