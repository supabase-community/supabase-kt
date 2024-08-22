package io.github.jan.supabase.realtime.event

import io.github.jan.supabase.logging.d
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.RealtimeMessage
import kotlinx.serialization.json.jsonPrimitive

/**
 * Event that handles the system reply event
 */
data object RSystemReplyEvent : RealtimeEvent {

    override fun handle(channel: RealtimeChannel, message: RealtimeMessage) {
        Realtime.logger.d { "Received system reply: ${message.payload}." }
        if(channel.status.value == RealtimeChannel.Status.UNSUBSCRIBING) {
            channel.updateStatus(RealtimeChannel.Status.UNSUBSCRIBED)
            Realtime.logger.d { "Unsubscribed from channel ${message.topic}" }
        }
    }

    override fun appliesTo(message: RealtimeMessage): Boolean {
        return message.event == RealtimeChannel.CHANNEL_EVENT_REPLY && message.payload["status"]?.jsonPrimitive?.content == "ok"
    }

}