package io.github.jan.supabase.realtime

import io.github.jan.supabase.annotations.SupabaseInternal
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Represents a message retrieved by the [RealtimeChannel]
 */
@Serializable
@SupabaseInternal
data class RealtimeMessage(val topic: String, val event: String, val payload: JsonObject, val ref: String?) {

    @Transient
    val eventType: EventType? = when {
        event == RealtimeChannel.CHANNEL_EVENT_SYSTEM && payload["status"]?.jsonPrimitive?.content == "ok" -> EventType.SYSTEM
        event == RealtimeChannel.CHANNEL_EVENT_REPLY && payload["response"]?.jsonObject?.containsKey(RealtimeChannel.CHANNEL_EVENT_POSTGRES_CHANGES) ?: false -> EventType.POSTGRES_SERVER_CHANGES
        event == RealtimeChannel.CHANNEL_EVENT_REPLY && payload["status"]?.jsonPrimitive?.content == "ok" -> EventType.SYSTEM_REPLY
        event == RealtimeChannel.CHANNEL_EVENT_POSTGRES_CHANGES -> EventType.POSTGRES_CHANGES
        event == RealtimeChannel.CHANNEL_EVENT_BROADCAST -> EventType.BROADCAST
        event == RealtimeChannel.CHANNEL_EVENT_CLOSE -> EventType.CLOSE
        event == RealtimeChannel.CHANNEL_EVENT_ERROR -> EventType.ERROR
        event == RealtimeChannel.CHANNEL_EVENT_PRESENCE_DIFF -> EventType.PRESENCE_DIFF
        event == RealtimeChannel.CHANNEL_EVENT_PRESENCE_STATE -> EventType.PRESENCE_STATE
        event == RealtimeChannel.CHANNEL_EVENT_SYSTEM && payload["message"]?.jsonPrimitive?.content?.contains("access token has expired") ?: false -> EventType.TOKEN_EXPIRED
        else -> null
    }

    enum class EventType {
        SYSTEM, SYSTEM_REPLY, POSTGRES_SERVER_CHANGES, POSTGRES_CHANGES, BROADCAST, CLOSE, ERROR, PRESENCE_DIFF, PRESENCE_STATE, TOKEN_EXPIRED
    }

}

