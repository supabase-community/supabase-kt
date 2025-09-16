package io.github.jan.supabase.realtime

import io.github.jan.supabase.SupabaseSerializer
import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.serializer.KotlinXSerializer
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentHashMapOf
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.plus
import kotlinx.serialization.json.JsonObject
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.fetchAndIncrement
import kotlin.concurrent.atomics.update

@SupabaseInternal
sealed class RealtimeCallbackId(val value: Int) {

    class Postgres(value: Int) : RealtimeCallbackId(value)

    class Presence(value: Int) : RealtimeCallbackId(value)

    class Broadcast(value: Int) : RealtimeCallbackId(value)

}

@SupabaseInternal
interface CallbackManager {

    fun triggerPostgresChange(ids: List<Int>, data: PostgresAction)

    fun triggerBroadcast(event: String, data: JsonObject)

    fun triggerPresenceDiff(joins: Map<String, Presence>, leaves: Map<String, Presence>)

    fun hasPresenceCallback(): Boolean

    fun addBroadcastCallback(event: String, callback: (JsonObject) -> Unit): RealtimeCallbackId.Broadcast

    fun addPostgresCallback(filter: PostgresJoinConfig, callback: (PostgresAction) -> Unit): RealtimeCallbackId.Postgres

    fun addPresenceCallback(callback: (PresenceAction) -> Unit): RealtimeCallbackId.Presence

    fun removeCallbackById(id: RealtimeCallbackId)

    fun setServerChanges(changes: List<PostgresJoinConfig>)

}

private typealias BroadcastMap = PersistentMap<String, PersistentList<RealtimeCallback.BroadcastCallback>>
private typealias PresenceMap = PersistentMap<Int, RealtimeCallback.PresenceCallback>
private typealias PostgresMap = PersistentMap<Int, RealtimeCallback.PostgresCallback>

internal class CallbackManagerImpl(
    private val serializer: SupabaseSerializer = KotlinXSerializer()
) : CallbackManager {

    private val nextId = AtomicInt(0)
    private val _serverChanges = AtomicReference(listOf<PostgresJoinConfig>())
    val serverChanges: List<PostgresJoinConfig> get() = _serverChanges.load()

    private val presenceCallbacks = AtomicReference<PresenceMap>(persistentHashMapOf())

    private val broadcastCallbacks = AtomicReference<BroadcastMap>(persistentHashMapOf())
    // Additional map to know from which list a callback may be removed in broadcastCallbacks without searching through the whole map
    private val broadcastEventId = AtomicReference<PersistentMap<Int, String>>(persistentHashMapOf())

    private val postgresCallbacks = AtomicReference<PostgresMap>(persistentHashMapOf())

    override fun addBroadcastCallback(event: String, callback: (JsonObject) -> Unit): RealtimeCallbackId.Broadcast {
        val id = nextId.fetchAndIncrement()
        broadcastCallbacks.update {
            val current = it[event] ?: persistentListOf()
            it.put(event, current + RealtimeCallback.BroadcastCallback(callback, event, id))
        }
        broadcastEventId.update {
            it.put(id, event)
        }
        return RealtimeCallbackId.Broadcast(id)
    }

    override fun addPostgresCallback(filter: PostgresJoinConfig, callback: (PostgresAction) -> Unit): RealtimeCallbackId.Postgres {
        val id = nextId.fetchAndIncrement()
        postgresCallbacks.update {
            it.put(id, RealtimeCallback.PostgresCallback(callback, filter, id))
        }
        return RealtimeCallbackId.Postgres(id)
    }

    override fun triggerPostgresChange(ids: List<Int>, data: PostgresAction) {
        val filter = serverChanges.filter { it.id in ids }
        val callbacks =
            postgresCallbacks.load().values.filter { cc -> filter.any { sc -> cc.filter == sc } }
        callbacks.forEach { it.callback(data) }
    }

    override fun triggerBroadcast(event: String, data: JsonObject) {
        broadcastCallbacks.load()[event]?.forEach { it.callback(data) }
    }

    override fun triggerPresenceDiff(joins: Map<String, Presence>, leaves: Map<String, Presence>) {
        presenceCallbacks.load().values.forEach { it.callback(PresenceActionImpl(serializer, joins, leaves)) }
    }

    override fun hasPresenceCallback(): Boolean {
        return presenceCallbacks.load().isNotEmpty()
    }

    override fun addPresenceCallback(callback: (PresenceAction) -> Unit):  RealtimeCallbackId.Presence {
        val id = nextId.fetchAndIncrement()
        presenceCallbacks.update {
            it.put(id, RealtimeCallback.PresenceCallback(callback, id))
        }
        return RealtimeCallbackId.Presence(id)
    }

    fun removeBroadcastCallbackById(id: Int) {
        val event = broadcastEventId.load()[id] ?: return
        broadcastCallbacks.update {
            it.put(event, it[event]?.removeAll { c -> c.id == id } ?: persistentListOf())
        }
        broadcastEventId.update {
            it.remove(id)
        }
    }

    fun removePresenceCallbackById(id: Int) {
        presenceCallbacks.update {
            it.remove(id)
        }
    }

    fun removePostgresCallbackById(id: Int) {
        postgresCallbacks.update {
            it.remove(id)
        }
    }

    override fun removeCallbackById(id: RealtimeCallbackId) {
        when (id) {
            is RealtimeCallbackId.Broadcast -> removeBroadcastCallbackById(id.value)
            is RealtimeCallbackId.Presence -> removePresenceCallbackById(id.value)
            is RealtimeCallbackId.Postgres -> removePostgresCallbackById(id.value)
        }
    }

    override fun setServerChanges(changes: List<PostgresJoinConfig>) {
        _serverChanges.store(changes)
    }

}