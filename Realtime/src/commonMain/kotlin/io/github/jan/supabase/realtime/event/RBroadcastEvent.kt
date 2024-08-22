package io.github.jan.supabase.realtime.event

import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.RealtimeMessage
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Handles broadcast events
 */
data object RBroadcastEvent : RealtimeEvent {

    override suspend fun handle(channel: RealtimeChannel, message: RealtimeMessage) {
        val event = message.payload["event"]?.jsonPrimitive?.content ?: ""
        channel.callbackManager.triggerBroadcast(event, message.payload["payload"]?.jsonObject ?: JsonObject(mutableMapOf()))
    }

    override fun appliesTo(message: RealtimeMessage): Boolean {
        return message.event == RealtimeChannel.CHANNEL_EVENT_BROADCAST
    }

}