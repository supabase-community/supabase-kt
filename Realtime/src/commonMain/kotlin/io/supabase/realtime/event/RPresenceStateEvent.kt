package io.supabase.realtime.event

import io.supabase.decodeIfNotEmptyOrDefault
import io.supabase.realtime.Presence
import io.supabase.realtime.RealtimeChannel
import io.supabase.realtime.RealtimeMessage

/**
 * Event that handles the presence state event
 */
data object RPresenceStateEvent : RealtimeEvent {

    override suspend fun handle(channel: RealtimeChannel, message: RealtimeMessage) {
        val joins = message.payload.decodeIfNotEmptyOrDefault(mapOf<String, Presence>())
        channel.callbackManager.triggerPresenceDiff(joins, mapOf())
    }

    override fun appliesTo(message: RealtimeMessage): Boolean {
        return message.event == RealtimeChannel.Companion.CHANNEL_EVENT_PRESENCE_STATE
    }

}