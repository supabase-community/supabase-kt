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
 * @param channelName the name of the channel to use for the realtime updates. If null, a channel name following the format "schema:table:id" will be generated
 */
inline fun <reified Data : Any> PostgrestQueryBuilder.selectSingleValueAsFlow(
    primaryKey: PrimaryKey<Data>,
    channelName: String? = null,
    crossinline filter: PostgrestFilterBuilder.() -> Unit
): Flow<Data> {
    val realtime = postgrest.supabaseClient.realtime as RealtimeImpl
    val id = realtime.nextIncrementId()
    val channel = realtime.channel(channelName ?: "$schema:$table:$id")
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
 * @param channelName the name of the channel to use for the realtime updates. If null, a channel name following the format "schema:table:id" will be generated
 */
inline fun <reified Data : Any, Value> PostgrestQueryBuilder.selectSingleValueAsFlow(
    primaryKey: KProperty1<Data, Value>,
    channelName: String? = null,
    crossinline filter: PostgrestFilterBuilder.() -> Unit
): Flow<Data> = selectSingleValueAsFlow(PrimaryKey(primaryKey.name) { primaryKey.get(it).toString() }, channelName, filter)

/**
 * Executes vertical filtering with select on [PostgrestQueryBuilder.table] and [PostgrestQueryBuilder.schema] and returns a [Flow] of a list of values matching the [filter].
 * This function listens for changes in the table and emits the new list whenever a change occurs.
 * @param primaryKey the primary key of the [Data] type
 * @param filter the filter to apply to the select query
 * @param channelName the name of the channel to use for the realtime updates. If null, a channel name following the format "schema:table:id" will be generated
 */
inline fun <reified Data : Any> PostgrestQueryBuilder.selectAsFlow(
    primaryKey: PrimaryKey<Data>,
    channelName: String? = null,
    filter: FilterOperation? = null,
): Flow<List<Data>> {
    val realtime = postgrest.supabaseClient.realtime as RealtimeImpl
    val id = realtime.nextIncrementId()
    val channel = realtime.channel(channelName ?: "$schema:$table:$id")
    return flow {
        val dataFlow = channel.postgresListDataFlow(
            schema = this@selectAsFlow.schema,
            table = this@selectAsFlow.table,
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
 * @param channelName the name of the channel to use for the realtime updates. If null, a channel name following the format "schema:table:id" will be generated
 */
inline fun <reified Data : Any, Value> PostgrestQueryBuilder.selectAsFlow(
    primaryKey: KProperty1<Data, Value>,
    channelName: String? = null,
    filter: FilterOperation? = null,
): Flow<List<Data>> = selectAsFlow(PrimaryKey(primaryKey.name) { primaryKey.get(it).toString() }, channelName, filter)