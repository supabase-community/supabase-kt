package io.github.jan.supabase.realtime

import io.github.jan.supabase.SupabaseSerializer
import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.collections.AtomicMutableList
import io.github.jan.supabase.serializer.KotlinXSerializer
import kotlinx.serialization.json.JsonObject
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.fetchAndIncrement

@SupabaseInternal
sealed interface CallbackManager {

    fun triggerPostgresChange(ids: List<Int>, data: PostgresAction)

    fun triggerBroadcast(event: String, data: JsonObject)

    fun triggerPresenceDiff(joins: Map<String, Presence>, leaves: Map<String, Presence>)

    fun addBroadcastCallback(event: String, callback: (JsonObject) -> Unit): Int

    fun addPostgresCallback(filter: PostgresJoinConfig, callback: (PostgresAction) -> Unit): Int

    fun addPresenceCallback(callback: (PresenceAction) -> Unit): Int

    fun removeCallbackById(id: Int)

    fun setServerChanges(changes: List<PostgresJoinConfig>)

    fun getCallbacks(): List<RealtimeCallback<*>>

}

internal class CallbackManagerImpl(
    private val serializer: SupabaseSerializer = KotlinXSerializer()
) : CallbackManager {

    private val nextId = AtomicInt(0)
    private val _serverChanges = AtomicReference(listOf<PostgresJoinConfig>())
    val serverChanges: List<PostgresJoinConfig> get() = _serverChanges.load()
    private val callbacks = AtomicMutableList<RealtimeCallback<*>>()

    override fun getCallbacks(): List<RealtimeCallback<*>> {
        return callbacks.toList()
    }

    override fun addBroadcastCallback(event: String, callback: (JsonObject) -> Unit): Int {
        val id = nextId.fetchAndIncrement()
        callbacks += RealtimeCallback.BroadcastCallback(callback, event, id)
        return id
    }

    override fun addPostgresCallback(filter: PostgresJoinConfig, callback: (PostgresAction) -> Unit): Int {
        val id = nextId.fetchAndIncrement()
        callbacks += RealtimeCallback.PostgresCallback(callback, filter, id)
        return id
    }

    override fun triggerPostgresChange(ids: List<Int>, data: PostgresAction) {
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
        presenceCallbacks.forEach { it.callback(PresenceActionImpl(serializer, joins, leaves)) }
    }

    override fun addPresenceCallback(callback: (PresenceAction) -> Unit): Int {
        val id = nextId.fetchAndIncrement()
        callbacks += RealtimeCallback.PresenceCallback(callback, id)
        return id
    }

    override fun removeCallbackById(id: Int) {
        callbacks.indexOfFirst { it.id == id }.takeIf { it != -1 }?.let { callbacks.removeAt(it) }
    }

    override fun setServerChanges(changes: List<PostgresJoinConfig>) {
        _serverChanges.store(changes)
    }

}