package io.supabase.realtime.event

import io.supabase.logging.e
import io.supabase.realtime.Realtime
import io.supabase.realtime.RealtimeChannel
import io.supabase.realtime.RealtimeMessage

/**
 * Event that handles an error event
 */
data object RErrorEvent : RealtimeEvent {

    override suspend fun handle(channel: RealtimeChannel, message: RealtimeMessage) {
        Realtime.Companion.logger.e { "Received an error in channel ${message.topic}. That could be as a result of an invalid access token" }
    }

    override fun appliesTo(message: RealtimeMessage): Boolean {
        return message.event == RealtimeChannel.Companion.CHANNEL_EVENT_ERROR
    }

}