package io.github.jan.supacompose.realtime

import kotlinx.atomicfu.atomic
import kotlinx.serialization.json.JsonObject

sealed interface CallbackManager {

    fun triggerPostgresChange(ids: List<Long>, data: PostgresAction)

    fun triggerBroadcast(event: String, data: JsonObject)

    fun addBroadcastCallback(event: String, callback: (JsonObject) -> Unit): Long

    fun addPostgresCallback(filter: PostgresJoinConfig, callback: (PostgresAction) -> Unit): Long

    fun removePostgresCallbackById(id: Long)

    fun removeBroadcastCallbackById(id: Long)

}

internal class CallbackManagerImpl : CallbackManager {

    private val _nextId = atomic(0L)
    var nextId: Long by _nextId //used to remove callbacks
    var serverChanges = listOf<PostgresJoinConfig>()
    val postgresCallbacks = mutableListOf<RealtimeCallback.PostgresCallback>()
    val broadcastCallbacks = mutableListOf<RealtimeCallback.BroadcastCallback>()

    override fun addBroadcastCallback(event: String, callback: (JsonObject) -> Unit): Long {
        val id = nextId++
        broadcastCallbacks += RealtimeCallback.BroadcastCallback(callback, event, id)
        return id
    }

    override fun addPostgresCallback(filter: PostgresJoinConfig, callback: (PostgresAction) -> Unit): Long {
        val id = nextId++
        postgresCallbacks += RealtimeCallback.PostgresCallback(callback, filter, id)
        return id
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

    override fun removePostgresCallbackById(id: Long) {
        postgresCallbacks.removeAll { it.id == id }
    }

    override fun removeBroadcastCallbackById(id: Long) {
        broadcastCallbacks.removeAll { it.id == id }
    }

}