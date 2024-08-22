package io.github.jan.supabase.realtime.event

import io.github.jan.supabase.decodeIfNotEmptyOrDefault
import io.github.jan.supabase.realtime.Presence
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.RealtimeMessage
import kotlinx.serialization.json.jsonObject

/**
 * Event that handles the presence diff event
 */
data object RPresenceDiffEvent : RealtimeEvent {

    override suspend fun handle(channel: RealtimeChannel, message: RealtimeMessage) {
        val joins = message.payload["joins"]?.jsonObject?.decodeIfNotEmptyOrDefault(mapOf<String, Presence>()) ?: emptyMap()
        val leaves = message.payload["leaves"]?.jsonObject?.decodeIfNotEmptyOrDefault(mapOf<String, Presence>()) ?: emptyMap()
        channel.callbackManager.triggerPresenceDiff(joins, leaves)
    }

    override fun appliesTo(message: RealtimeMessage): Boolean {
        return message.event == RealtimeChannel.CHANNEL_EVENT_PRESENCE_DIFF
    }

}