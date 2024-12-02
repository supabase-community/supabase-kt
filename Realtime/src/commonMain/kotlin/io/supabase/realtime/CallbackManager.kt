package io.supabase.realtime

import io.supabase.SupabaseSerializer
import io.supabase.annotations.SupabaseInternal
import io.supabase.collections.AtomicMutableList
import io.supabase.serializer.KotlinXSerializer
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

}

internal class CallbackManagerImpl(
    private val serializer: SupabaseSerializer = KotlinXSerializer()
) : CallbackManager {

    private var nextId by atomic(0L)
    private var _serverChanges by atomic(listOf<PostgresJoinConfig>())
    val serverChanges: List<PostgresJoinConfig> get() = _serverChanges
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
        presenceCallbacks.forEach { it.callback(PresenceActionImpl(serializer, joins, leaves)) }
    }

    override fun addPresenceCallback(callback: (PresenceAction) -> Unit): Long {
        val id = nextId++
        callbacks += RealtimeCallback.PresenceCallback(callback, id)
        return id
    }

    override fun removeCallbackById(id: Long) {
        callbacks.indexOfFirst { it.id == id }.takeIf { it != -1 }?.let { callbacks.removeAt(it) }
    }

    override fun setServerChanges(changes: List<PostgresJoinConfig>) {
        _serverChanges = changes
    }

}