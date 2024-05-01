package io.github.jan.supabase.realtime

import io.github.jan.supabase.postgrest.query.PostgrestQueryBuilder
import io.github.jan.supabase.postgrest.query.filter.FilterOperation
import io.github.jan.supabase.postgrest.query.filter.PostgrestFilterBuilder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlin.reflect.KProperty1

/**
 * Executes vertical filtering with select on [PostgrestQueryBuilder.table] and [PostgrestQueryBuilder.schema] and returns a [Flow] of a single value matching the [filter].
 * This function listens for changes in the table and emits the new value whenever a change occurs.
 * @param primaryKey the primary key of the [Data] type
 * @param filter the filter to apply to the select query
 */
inline fun <reified Data : Any> PostgrestQueryBuilder.selectSingleValueAsFlow(
    primaryKey: PrimaryKey<Data>,
    crossinline filter: PostgrestFilterBuilder.() -> Unit
): Flow<Data> {
    val realtime = postgrest.supabaseClient.realtime as RealtimeImpl
    val id = realtime.nextIncrementId()
    val channel = realtime.channel("$schema:$table:$id")
    return flow {
        val dataFlow = channel.postgresSingleDataFlow(
            schema = this@selectSingleValueAsFlow.schema,
            table = this@selectSingleValueAsFlow.table,
            primaryKey = primaryKey,
            filter = filter
        )
        channel.subscribe()
        emitAll(dataFlow)
    }.onCompletion {
        realtime.removeChannel(channel)
    }
}

/**
 * Executes vertical filtering with select on [PostgrestQueryBuilder.table] and [PostgrestQueryBuilder.schema] and returns a [Flow] of a single value matching the [filter].
 * This function listens for changes in the table and emits the new value whenever a change occurs.
 * @param primaryKey the primary key of the [Data] type
 * @param filter the filter to apply to the select query
 */
inline fun <reified Data : Any, Value> PostgrestQueryBuilder.selectSingleValueAsFlow(
    primaryKey: KProperty1<Data, Value>,
    crossinline filter: PostgrestFilterBuilder.() -> Unit
): Flow<Data> = selectSingleValueAsFlow(PrimaryKey(primaryKey.name) { primaryKey.get(it).toString() }, filter)

/**
 * Executes vertical filtering with select on [PostgrestQueryBuilder.table] and [PostgrestQueryBuilder.schema] and returns a [Flow] of a list of values matching the [filter].
 * This function listens for changes in the table and emits the new list whenever a change occurs.
 * @param primaryKey the primary key of the [Data] type
 * @param filter the filter to apply to the select query
 */
inline fun <reified Data : Any> PostgrestQueryBuilder.selectListAsFlow(
    primaryKey: PrimaryKey<Data>,
    filter: FilterOperation? = null,
): Flow<List<Data>> {
    val realtime = postgrest.supabaseClient.realtime as RealtimeImpl
    val id = realtime.nextIncrementId()
    val channel = realtime.channel("$schema:$table:$id")
    return flow {
        val dataFlow = channel.postgresListDataFlow(
            schema = this@selectListAsFlow.schema,
            table = this@selectListAsFlow.table,
            primaryKey = primaryKey,
            filter = filter
        )
        channel.subscribe()
        emitAll(dataFlow)
    }.onCompletion {
        realtime.removeChannel(channel)
    }
}

/**
 * Executes vertical filtering with select on [PostgrestQueryBuilder.table] and [PostgrestQueryBuilder.schema] and returns a [Flow] of a list of values matching the [filter].
 * This function listens for changes in the table and emits the new list whenever a change occurs.
 * @param primaryKey the primary key of the [Data] type
 * @param filter the filter to apply to the select query
 */
inline fun <reified Data : Any, Value> PostgrestQueryBuilder.selectListAsFlow(
    primaryKey: KProperty1<Data, Value>,
    filter: FilterOperation? = null,
): Flow<List<Data>> = selectListAsFlow(PrimaryKey(primaryKey.name) { primaryKey.get(it).toString() }, filter)