package io.github.jan.supabase.realtime.event

import io.github.jan.supabase.logging.e
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.RealtimeMessage

/**
 * Event that handles an error event
 */
data object RErrorEvent : RealtimeEvent {

    override fun handle(channel: RealtimeChannel, message: RealtimeMessage) {
        Realtime.logger.e { "Received an error in channel ${message.topic}. That could be as a result of an invalid access token" }
    }

    override fun appliesTo(message: RealtimeMessage): Boolean {
        return message.event == RealtimeChannel.CHANNEL_EVENT_ERROR
    }

}