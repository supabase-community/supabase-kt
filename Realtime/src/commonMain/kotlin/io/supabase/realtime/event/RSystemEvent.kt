package io.supabase.realtime.event

import io.supabase.logging.d
import io.supabase.realtime.Realtime
import io.supabase.realtime.RealtimeChannel
import io.supabase.realtime.RealtimeMessage
import kotlinx.serialization.json.jsonPrimitive

/**
 * Event that handles the system event
 */
data object RSystemEvent : RealtimeEvent {

    override suspend fun handle(channel: RealtimeChannel, message: RealtimeMessage) {
        Realtime.Companion.logger.d { "Subscribed to channel ${message.topic}" }
        channel.updateStatus(RealtimeChannel.Status.SUBSCRIBED)
    }

    override fun appliesTo(message: RealtimeMessage): Boolean {
        return  message.event == RealtimeChannel.Companion.CHANNEL_EVENT_SYSTEM && message.payload["status"]?.jsonPrimitive?.content == "ok"
    }

}
