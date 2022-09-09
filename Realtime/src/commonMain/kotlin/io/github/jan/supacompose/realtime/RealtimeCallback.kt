package io.github.jan.supacompose.realtime

import kotlinx.serialization.json.JsonObject

sealed interface RealtimeCallback <T, F> {

    val callback: (T) -> Unit
    val filter: F

    class PostgresCallback(
        override val callback: (PostgresAction) -> Unit,
        override val filter: PostgresJoinConfig
    ): RealtimeCallback<PostgresAction, PostgresJoinConfig>

    class BroadcastCallback(
        override val callback: (JsonObject) -> Unit,
        override val filter: String
    ): RealtimeCallback<JsonObject, String>

}