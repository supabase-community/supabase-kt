package io.github.jan.supacompose.realtime

import kotlinx.serialization.json.JsonObject

sealed interface CallbackManager {

    fun triggerPostgresChange(ids: List<Long>, data: PostgresAction)

    fun triggerBroadcast(event: String, data: JsonObject)

    fun addBroadcastCallback(event: String, callback: (JsonObject) -> Unit)

    fun addPostgresCallback(filter: PostgresJoinConfig, callback: (PostgresAction) -> Unit)

}

internal class CallbackManagerImpl : CallbackManager {

    var serverChanges = listOf<PostgresJoinConfig>()
    val postgresCallbacks = mutableListOf<RealtimeCallback.PostgresCallback>()
    val broadcastCallbacks = mutableListOf<RealtimeCallback.BroadcastCallback>()

    override fun addBroadcastCallback(event: String, callback: (JsonObject) -> Unit) {
        broadcastCallbacks += RealtimeCallback.BroadcastCallback(callback, event)
    }

    override fun addPostgresCallback(filter: PostgresJoinConfig, callback: (PostgresAction) -> Unit) {
        postgresCallbacks += RealtimeCallback.PostgresCallback(callback, filter)
    }

    override fun triggerPostgresChange(ids: List<Long>, data: PostgresAction) {
        val filter = serverChanges.filter { it.id in ids }
        val callbacks =
            postgresCallbacks.filter { cc -> filter.any { sc -> cc.filter == sc } }
        callbacks.forEach { it.callback(data) }
    }

    override fun triggerBroadcast(event: String, data: JsonObject) {
        val callbacks = broadcastCallbacks.filter { it.filter == event }
        callbacks.forEach { it.callback(data) }
    }

}