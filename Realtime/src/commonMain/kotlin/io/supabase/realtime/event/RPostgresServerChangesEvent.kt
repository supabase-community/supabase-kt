package io.supabase.realtime.event

import io.supabase.logging.d
import io.supabase.realtime.PostgresJoinConfig
import io.supabase.realtime.Realtime
import io.supabase.realtime.RealtimeChannel
import io.supabase.realtime.RealtimeMessage
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

/**
 * Event that handles the server changes
 */
data object RPostgresServerChangesEvent : RealtimeEvent {

    override suspend fun handle(channel: RealtimeChannel, message: RealtimeMessage) {
        val serverPostgresChanges = message.payload["response"]?.jsonObject?.get("postgres_changes")?.jsonArray?.let { Json.decodeFromJsonElement<List<PostgresJoinConfig>>(it) } ?: listOf() //server postgres changes
        channel.callbackManager.setServerChanges(serverPostgresChanges)
        if(channel.status.value != RealtimeChannel.Status.SUBSCRIBED) {
            Realtime.Companion.logger.d { "Joined channel ${message.topic}" }
            channel.updateStatus(RealtimeChannel.Status.SUBSCRIBED)
        }
    }

    override fun appliesTo(message: RealtimeMessage): Boolean {
        return message.event == RealtimeChannel.Companion.CHANNEL_EVENT_REPLY && message.payload["response"]?.jsonObject?.containsKey(
            RealtimeChannel.Companion.CHANNEL_EVENT_POSTGRES_CHANGES
        ) ?: false
    }

}
