package io.github.jan.supacompose.realtime

import kotlinx.serialization.json.JsonObject

sealed interface RealtimeCallback <T, F> {

    val callback: (T) -> Unit
    val filter: F
    val id: Long

    class PostgresCallback(
        override val callback: (PostgresAction) -> Unit,
        override val filter: PostgresJoinConfig,
        override val id: Long
    ): RealtimeCallback<PostgresAction, PostgresJoinConfig>

    class BroadcastCallback(
        override val callback: (JsonObject) -> Unit,
        override val filter: String,
        override val id: Long
    ): RealtimeCallback<JsonObject, String>

}