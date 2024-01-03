package io.github.jan.supabase.realtime

import io.github.jan.supabase.collections.AtomicMutableMap
import io.github.jan.supabase.exceptions.NotFoundRestException
import io.github.jan.supabase.exceptions.UnknownRestException
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.filter.FilterOperation
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.postgrest.query.filter.PostgrestFilterBuilder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.json.jsonPrimitive
import kotlin.reflect.KProperty1

data class PrimaryKey<Data>(val columnName: String, val producer: (Data) -> String)

inline fun <reified Data> RealtimeChannel.presenceDataFlow(): Flow<List<Data>> {
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

inline fun <reified Data : Any> RealtimeChannel.postgresListDataFlow(
    schema: String = "public",
    table: String,
    filter: FilterOperation? = null,
    primaryKey: PrimaryKey<Data>
): Flow<List<Data>> = callbackFlow {
    val cache = AtomicMutableMap<String, Data>()
    try {
        val result = supabaseClient.postgrest.from(schema, table).select()
        val data = result.decodeList<Data>()
        data.forEach {
            val key = primaryKey.producer(it)
            cache[key] = it
        }
        trySend(data)
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
                    val key = primaryKey.producer(data)
                    cache[key] = data
                }
                is PostgresAction.Update -> {
                    val data = it.decodeRecord<Data>()
                    val key = primaryKey.producer(data)
                    cache[key] = data
                }
                is PostgresAction.Delete -> {
                    cache.remove(it.oldRecord[primaryKey.columnName]?.jsonPrimitive?.content ?: error("No primary key found"))
                }
                else -> {}
            }
            trySend(cache.values.toList())
        }
        close()
    }
}

inline fun <reified Data : Any, Value> RealtimeChannel.postgresListDataFlow(
    schema: String = "public",
    table: String,
    filter: FilterOperation? = null,
    primaryKey: KProperty1<Data, Value>,
): Flow<List<Data>> = postgresListDataFlow<Data>(
    filter = filter,
    table = table,
    schema = schema,
    primaryKey = PrimaryKey(
        primaryKey.name
    ){
        primaryKey.get(it).toString()
    }
)


inline fun <reified Data : Any> RealtimeChannel.postgresSingleDataFlow(
    schema: String = "public",
    table: String,
    crossinline filter: PostgrestFilterBuilder.() -> Unit,
    primaryKey: PrimaryKey<Data>
): Flow<Data> = callbackFlow {
    val key = try {
        val result = supabaseClient.postgrest.from(schema, table).select {
            limit(1)
            single()
            filter(filter)
        }
        val data = result.decodeAs<Data>()
        trySend(data)
        primaryKey.producer(data)
    } catch (e: UnknownRestException) {
        close(IllegalStateException("Data matching filter and table name $table not found"))
        return@callbackFlow
    }
    val changeFlow = postgresChangeFlow<PostgresAction>(schema) {
        this.table = table
        filter(primaryKey.columnName, FilterOperator.EQ, key)
    }
    launch {
        changeFlow.collect {
            when (it) {
                is PostgresAction.Insert -> {
                    val data = it.decodeRecord<Data>()
                    trySend(data)
                }
                is PostgresAction.Update -> {
                    val data = it.decodeRecord<Data>()
                    trySend(data)
                }
                is PostgresAction.Delete -> {
                    close()
                }
                else -> {}
            }
        }
        close()
    }
}

inline fun <reified Data, Value> RealtimeChannel.postgresSingleDataFlow(
    schema: String = "public",
    table: String,
    primaryKey: KProperty1<Data, Value>,
    crossinline filter: PostgrestFilterBuilder.() -> Unit
): Flow<Data> = postgresSingleDataFlow(
    schema = schema,
    table = table,
    filter = filter,
    primaryKey = PrimaryKey(primaryKey.name) {
        primaryKey.get(it).toString()
    }
)