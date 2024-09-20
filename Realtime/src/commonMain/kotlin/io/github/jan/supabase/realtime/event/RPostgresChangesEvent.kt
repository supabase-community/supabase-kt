package io.github.jan.supabase.realtime.event

import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.RealtimeMessage
import io.github.jan.supabase.realtime.data.PostgresActionData
import io.github.jan.supabase.realtime.realtime
import io.github.jan.supabase.supabaseJson
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull

/**
 * Handles postgres changes events
 */
data object RPostgresChangesEvent : RealtimeEvent {

    override suspend fun handle(channel: RealtimeChannel, message: RealtimeMessage) {
        val data = message.payload["data"]?.jsonObject ?: return
        val ids = message.payload["ids"]?.jsonArray?.mapNotNull { it.jsonPrimitive.longOrNull } ?: emptyList() //the ids of the matching postgres changes
        val postgresAction = supabaseJson.decodeFromJsonElement<PostgresActionData>(data)
        val action = when(data["type"]?.jsonPrimitive?.content ?: "") {
            "UPDATE" -> PostgresAction.Update(
                postgresAction.record ?: error("Received no record on update event"),
                postgresAction.oldRecord ?: error("Received no old record on update event"),
                postgresAction.columns,
                postgresAction.commitTimestamp,
                channel.supabaseClient.realtime.serializer
            )
            "DELETE" -> PostgresAction.Delete(
                postgresAction.oldRecord ?: error("Received no old record on delete event"),
                postgresAction.columns,
                postgresAction.commitTimestamp,
                channel.supabaseClient.realtime.serializer
            )
            "INSERT" -> PostgresAction.Insert(
                postgresAction.record ?: error("Received no record on update event"),
                postgresAction.columns,
                postgresAction.commitTimestamp,
                channel.supabaseClient.realtime.serializer
            )
            "SELECT" -> PostgresAction.Select(
                postgresAction.record ?: error("Received no record on update event"),
                postgresAction.columns,
                postgresAction.commitTimestamp,
                channel.supabaseClient.realtime.serializer
            )
            else -> error("Unknown event type ${message.event}")
        }
        channel.callbackManager.triggerPostgresChange(ids, action)
    }

    override fun appliesTo(message: RealtimeMessage): Boolean {
        return message.event == RealtimeChannel.CHANNEL_EVENT_POSTGRES_CHANGES
    }

}