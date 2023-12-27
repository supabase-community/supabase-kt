package io.github.jan.supabase.realtime

import io.github.jan.supabase.collections.AtomicMutableMap
import io.github.jan.supabase.exceptions.NotFoundRestException
import io.github.jan.supabase.exceptions.UnknownRestException
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.filter.FilterOperation
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.postgrest.query.filter.PostgrestFilterBuilder
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.reflect.KProperty1

inline fun <reified Data> RealtimeChannel.presenceFlow(): Flow<List<Data>> {
    val cache = AtomicMutableMap<String, Data>()
    return presenceChangeFlow().map {
        it.joins.forEach { (key, presence) ->
            cache[key] = presence.stateAs<Data>(supabaseClient.realtime.serializer)
        }
        it.leaves.forEach { (key, _) ->
            cache.remove(key)
        }
        cache.values.toList()
    }
}

inline fun <reified Data : Any> RealtimeChannel.postgresListFlow(
    schema: String = "public",
    table: String,
    filter: FilterOperation? = null,
    crossinline primaryKey: (Data) -> String,
): Flow<List<Data>> = flow {
    val cache = AtomicMutableMap<String, Data>()
    try {
        val result = supabaseClient.postgrest.from(schema, table).select()
        val data = result.decodeList<Data>()
        data.forEach {
            val key = primaryKey(it)
            cache[key] = it
        }
        emit(data)
    } catch (e: NotFoundRestException) {
        close(IllegalStateException("Table with name $table not found"))
        return@callbackFlow
    }
    val changeFlow = postgresChangeFlow<PostgresAction>(schema) {
        this.table = table
        filter?.let {
            filter(it)
        }
    }
    launch {
        changeFlow.collect {
            when (it) {
                is PostgresAction.Insert -> {
                    val data = it.decodeRecord<Data>()
                    val key = primaryKey(data)
                    cache[key] = data
                }
                is PostgresAction.Update -> {
                    val data = it.decodeRecord<Data>()
                    val key = primaryKey(data)
                    cache[key] = data
                }
                is PostgresAction.Delete -> {
                    val data = it.decodeRecord<Data>()
                    val key = primaryKey(data)
                    cache.remove(key)
                }
                else -> {}
            }
            trySend(cache.values.toList())
        }
    }
    awaitClose {
        launch {
            changeFlow.()
        }
    }
}

inline fun <V> RealtimeChannel.listFlow(
    filter: FilterOperation? = null,
    primaryKey: KProperty1<Data, V>,
): Flow<List<Data>> = listFlow(
    filter = filter,
    primaryKey = {
        primaryKey.get(it).toString()
    }
)

inline fun RealtimeChannel.singleFlow(
    crossinline filter: PostgrestFilterBuilder.() -> Unit,
    crossinline primaryKey: (Data) -> PrimaryKey
): Flow<Data> = callbackFlow {
    val key = try {
        val result = supabaseClient.postgrest.from(schema, table).select {
            limit(1)
            single()
            filter(filter)
        }
        val data = decodeData(result.data)
        trySend(data)
        primaryKey(data)
    } catch (e: UnknownRestException) {
        close(IllegalStateException("Data matching filter and table name $table not found"))
        return@callbackFlow
    }
    val channel = supabaseClient.realtime.channel(channelId)
    val changeFlow = channel.postgresChangeFlow<PostgresAction>(schema) {
        this.table = this@RealtimeTable.table
        filter(key.columnName, FilterOperator.EQ, key.value)
    }
    launch {
        changeFlow.collect {
            when (it) {
                is PostgresAction.Insert -> {
                    val data = decodeData(it.record.toString())
                    trySend(data)
                }
                is PostgresAction.Update -> {
                    val data = decodeData(it.record.toString())
                    trySend(data)
                }
                is PostgresAction.Delete -> {
                    close()
                }
                else -> {}
            }
        }
    }
    channel.subscribe()
    awaitClose {
        launch {
            supabaseClient.realtime.removeChannel(channel)
        }
    }
}

inline fun <V> RealtimeChannel.singleFlow(
    primaryKey: KProperty1<Data, V>,
    crossinline filter: PostgrestFilterBuilder.() -> Unit
): Flow<Data> = singleFlow(
    filter = filter,
    primaryKey = {
        PrimaryKey(primaryKey.name, primaryKey.get(it).toString())
    }
)