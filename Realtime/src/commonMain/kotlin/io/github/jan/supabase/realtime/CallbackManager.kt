package io.github.jan.supabase.realtime

import co.touchlab.stately.collections.IsoMutableList
import io.github.jan.supabase.annotiations.SupabaseInternal
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

}

internal class CallbackManagerImpl : CallbackManager {

    private var nextId by atomic(0L)
    var serverChanges = listOf<PostgresJoinConfig>()
    private val callbacks = IsoMutableList<RealtimeCallback<*>>()

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
        val filter = serverChanges.filter { it.id in ids }
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
        presenceCallbacks.forEach { it.callback(PresenceActionImpl(joins, leaves)) }
    }

    override fun addPresenceCallback(callback: (PresenceAction) -> Unit): Long {
        val id = nextId++
        callbacks += RealtimeCallback.PresenceCallback(callback, id)
        return id
    }

    override fun removeCallbackById(id: Long) {
        callbacks.removeAll { it.id == id }
    }

}