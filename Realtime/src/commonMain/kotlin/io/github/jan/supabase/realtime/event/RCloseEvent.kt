package io.github.jan.supabase.realtime.event

import io.github.jan.supabase.logging.d
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.RealtimeMessage

/**
 * Event that handles the closing of a channel
 */
data object RCloseEvent : RealtimeEvent {

    override suspend fun handle(channel: RealtimeChannel, message: RealtimeMessage) {
        channel.realtime.removeChannel(channel)
        Realtime.logger.d { "Unsubscribed from channel ${message.topic}" }
    }

    override fun appliesTo(message: RealtimeMessage): Boolean {
        return message.event == RealtimeChannel.CHANNEL_EVENT_CLOSE
    }

}