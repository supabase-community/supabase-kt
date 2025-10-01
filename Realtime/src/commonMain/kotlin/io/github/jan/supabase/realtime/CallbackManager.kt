package io.github.jan.supabase.realtime

import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.collections.AtomicMutableList
import kotlinx.atomicfu.atomic
import kotlinx.serialization.json.JsonObject

@SupabaseInternal
sealed interface CallbackManager {

    fun triggerPostgresChange(ids: List<Long>, data: PostgresAction)

    fun triggerBroadcast(event: String, data: JsonObject)

    fun triggerPresenceDiff(joins: Map<String, Presence>, leaves: Map<String, Presence>)

    fun addBroadcastCallback(event: String, callback: (JsonObject) -> Unit): Long

    fun addPostgresCallback(filter: PostgresJoinConfig, callback: (PostgresAction) -> Unit): Long

    fun addPresenceCallback(callback: (PresenceAction) -> Unit): Long

    fun removeCallbackById(id: Long)

    fun setServerChanges(changes: List<PostgresJoinConfig>)

    fun hasPresenceCallbacks(): Boolean

}

internal class CallbackManagerImpl(
    private val realtime: Realtime
) : CallbackManager {

    private var nextId by atomic(0L)
    private var _serverChanges by atomic(listOf<PostgresJoinConfig>())
    private val callbacks = AtomicMutableList<RealtimeCallback<*>>()

    override fun addBroadcastCallback(event: String, callback: (JsonObject) -> Unit): Long {
        val id = nextId++
        callbacks += RealtimeCallback.BroadcastCallback(callback, event, id)
        return id
    }

    override fun addPostgresCallback(filter: PostgresJoinConfig, callback: (PostgresAction) -> Unit): Long {
        val id = nextId++
        callbacks += RealtimeCallback.PostgresCallback(callback, filter, id)
        return id
    }

    override fun triggerPostgresChange(ids: List<Long>, data: PostgresAction) {
        val filter = _serverChanges.filter { it.id in ids }
        val postgresCallbacks = callbacks.filterIsInstance<RealtimeCallback.PostgresCallback>()
        val callbacks =
            postgresCallbacks.filter { cc -> filter.any { sc -> cc.filter == sc } }
        callbacks.forEach { it.callback(data) }
    }

    override fun triggerBroadcast(event: String, data: JsonObject) {
        val broadcastCallbacks = callbacks.filterIsInstance<RealtimeCallback.BroadcastCallback>()
        val callbacks = broadcastCallbacks.filter { it.event == event }
        callbacks.forEach { it.callback(data) }
    }

    override fun triggerPresenceDiff(joins: Map<String, Presence>, leaves: Map<String, Presence>) {
        val presenceCallbacks = callbacks.filterIsInstance<RealtimeCallback.PresenceCallback>()
        presenceCallbacks.forEach { it.callback(PresenceActionImpl(realtime.serializer, joins, leaves)) }
    }

    override fun addPresenceCallback(callback: (PresenceAction) -> Unit): Long {
        val id = nextId++
        callbacks += RealtimeCallback.PresenceCallback(callback, id)
        return id
    }

    override fun hasPresenceCallbacks(): Boolean {
        return callbacks.any { it is RealtimeCallback.PresenceCallback }
    }

    override fun removeCallbackById(id: Long) {
        callbacks.removeAll { it.id == id }
    }

    override fun setServerChanges(changes: List<PostgresJoinConfig>) {
        _serverChanges = changes
    }

}
