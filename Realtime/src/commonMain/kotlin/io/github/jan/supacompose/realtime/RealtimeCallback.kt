package io.github.jan.supacompose.realtime

import kotlinx.serialization.json.JsonObject

sealed interface RealtimeCallback <T> {

    val callback: (T) -> Unit
    val id: Long

    class PostgresCallback(
        override val callback: (PostgresAction) -> Unit,
        val filter: PostgresJoinConfig,
        override val id: Long
    ): RealtimeCallback<PostgresAction>

    class BroadcastCallback(
        override val callback: (JsonObject) -> Unit,
        val event: String,
        override val id: Long
    ): RealtimeCallback<JsonObject>

    class PresenceCallback(
        override val callback: (PresenceAction) -> Unit,
        override val id: Long
    ): RealtimeCallback<PresenceAction>

}