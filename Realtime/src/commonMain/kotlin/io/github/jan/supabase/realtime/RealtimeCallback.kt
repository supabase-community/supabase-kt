package io.github.jan.supabase.realtime

import io.github.jan.supabase.annotations.SupabaseInternal
import kotlinx.serialization.json.JsonObject

@SupabaseInternal
sealed interface RealtimeCallback <T> {

    val callback: (T) -> Unit
    val id: Int

    class PostgresCallback(
        override val callback: (PostgresAction) -> Unit,
        val filter: PostgresJoinConfig,
        override val id: Int
    ): RealtimeCallback<PostgresAction>

    class BroadcastCallback(
        override val callback: (JsonObject) -> Unit,
        val event: String,
        override val id: Int
    ): RealtimeCallback<JsonObject>

    class PresenceCallback(
        override val callback: (PresenceAction) -> Unit,
        override val id: Int
    ): RealtimeCallback<PresenceAction>

}