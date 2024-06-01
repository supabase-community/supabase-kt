@file:Suppress("MatchingDeclarationName")

package io.github.jan.supabase.realtime

import io.github.jan.supabase.collections.AtomicMutableMap
import io.github.jan.supabase.exceptions.NotFoundRestException
import io.github.jan.supabase.exceptions.UnknownRestException
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.filter.FilterOperation
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.postgrest.query.filter.PostgrestFilterBuilder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.jsonPrimitive
import kotlin.jvm.JvmName
import kotlin.reflect.KProperty1

/**
 * Represents the primary key of the [Data] type.
 * @param columnName the column name of the primary key
 * @param producer a function that produces the primary key value from the [Data] object
 */
data class PrimaryKey<Data>(val columnName: String, val producer: (Data) -> String)

@PublishedApi
internal fun <Data> List<PrimaryKey<Data>>.producer(data: Data): String =
    fold("") { value, pk -> value + pk.producer(data) }

@PublishedApi
internal val <Data> List<PrimaryKey<Data>>.columnName: String
    get() = fold("") { value, pk -> value + pk.columnName }

/**
 * Listens for presence changes and caches the presences based on their keys. This function automatically handles joins and leaves.
 *
 * If you want more control, use the [presenceChangeFlow] function.
 * @return a [Flow] of the current presences in a list. This list is updated and emitted whenever a presence joins or leaves.
 */
inline fun <reified Data> RealtimeChannel.presenceDataFlow(): Flow<List<Data>> {
    val cache = AtomicMutableMap<String, Data>()
    return presenceChangeFlow().map {
        // order matters here, leaves events must happen first for updates to work properly
        it.leaves.forEach { (key, _) ->
            cache.remove(key)
        }
        it.joins.forEach { (key, presence) ->
            cache[key] = presence.stateAs<Data>(supabaseClient.realtime.serializer)
        }
        cache.values.toList()
    }
}

/**
 * This function retrieves the initial data from the table and then listens for changes. It automatically handles inserts, updates and deletes.
 *
 * If you want more control, use the [postgresChangeFlow] function.
 * @param schema the schema of the table
 * @param table the table name
 * @param filter an optional filter to filter the data
 * @param primaryKey the primary key of the [Data] type
 * @return a [Flow] of the current data in a list. This list is updated and emitted whenever a change occurs.
 */
inline fun <reified Data : Any> RealtimeChannel.postgresListDataFlow(
    schema: String = "public",
    table: String,
    filter: FilterOperation? = null,
    primaryKey: PrimaryKey<Data>
): Flow<List<Data>> = postgresListDataFlow(schema, table, filter, listOf(primaryKey))

/**
 * This function retrieves the initial data from the table and then listens for changes. It automatically handles inserts, updates and deletes.
 *
 * If you want more control, use the [postgresChangeFlow] function.
 * @param schema the schema of the table
 * @param table the table name
 * @param filter an optional filter to filter the data
 * @param primaryKeys the list of primary key of the [Data] type
 * @return a [Flow] of the current data in a list. This list is updated and emitted whenever a change occurs.
 */
inline fun <reified Data : Any> RealtimeChannel.postgresListDataFlow(
    schema: String = "public",
    table: String,
    filter: FilterOperation? = null,
    primaryKeys: List<PrimaryKey<Data>>
): Flow<List<Data>> {
    val cache = AtomicMutableMap<String, Data>()
    val changeFlow = postgresChangeFlow<PostgresAction>(schema) {
        this.table = table
        filter?.let {
            filter(it)
        }
    }
    return channelFlow {
        val initialData = try {
            val result = supabaseClient.postgrest.from(schema, table).select {
                filter?.let {
                    filter {
                        this.filter(it)
                    }
                }
            }
            val data = result.decodeList<Data>()
            data.forEach {
                val key = primaryKeys.producer(it)
                cache[key] = it
            }
            data
        } catch (e: NotFoundRestException) {
            error("Table with name $table not found")
        }
        trySend(initialData)
        changeFlow.collect {
            when (it) {
                is PostgresAction.Insert -> {
                    val data = it.decodeRecord<Data>()
                    val key = primaryKeys.producer(data)
                    cache[key] = data
                }

                is PostgresAction.Update -> {
                    val data = it.decodeRecord<Data>()
                    val key = primaryKeys.producer(data)
                    cache[key] = data
                }

                is PostgresAction.Delete -> {
                    cache.remove(
                        primaryKeys.map { key ->
                            it.oldRecord[key.columnName]?.jsonPrimitive?.content
                        }.joinToString { "" }
                    )
                }

                else -> {}
            }
            trySend(cache.values.toList())
        }
    }
}

/**
 * This function retrieves the initial data from the table and then listens for changes. It automatically handles inserts, updates and deletes.
 *
 * If you want more control, use the [postgresChangeFlow] function.
 * @param schema the schema of the table
 * @param table the table name
 * @param filter an optional filter to filter the data
 * @param primaryKey the primary key of the [Data] type
 * @return a [Flow] of the current data in a list. This list is updated and emitted whenever a change occurs.
 */
inline fun <reified Data : Any, Value> RealtimeChannel.postgresListDataFlow(
    schema: String = "public",
    table: String,
    filter: FilterOperation? = null,
    primaryKey: KProperty1<Data, Value>,
): Flow<List<Data>> = postgresListDataFlow(schema, table, filter, listOf(primaryKey))

/**
 * This function retrieves the initial data from the table and then listens for changes. It automatically handles inserts, updates and deletes.
 *
 * If you want more control, use the [postgresChangeFlow] function.
 * @param schema the schema of the table
 * @param table the table name
 * @param filter an optional filter to filter the data
 * @param primaryKeys the list of primary keys of the [Data] type
 * @return a [Flow] of the current data in a list. This list is updated and emitted whenever a change occurs.
 */
@JvmName("postgresListDataFlowMultiplePks")
inline fun <reified Data : Any, Value> RealtimeChannel.postgresListDataFlow(
    schema: String = "public",
    table: String,
    filter: FilterOperation? = null,
    primaryKeys: List<KProperty1<Data, Value>>,
): Flow<List<Data>> = postgresListDataFlow<Data>(
    filter = filter,
    table = table,
    schema = schema,
    primaryKeys = primaryKeys.map { primaryKey ->
        PrimaryKey(primaryKey.name) { primaryKey.get(it).toString() }
    }
)

/**
 * This function retrieves the initial data for a single value and then listens for changes on that value. It automatically handles updates and closes the flow on the delete event.
 *
 * If you want more control, use the [postgresChangeFlow] function.
 * @param schema the schema of the table
 * @param table the table name
 * @param filter filter the the value you want to listen to
 * @param primaryKey the primary key of the [Data] type
 * @return a [Flow] of the current data. This flow emits a new value whenever a change occurs.
 */
suspend inline fun <reified Data : Any> RealtimeChannel.postgresSingleDataFlow(
    schema: String = "public",
    table: String,
    primaryKey: PrimaryKey<Data>,
    crossinline filter: PostgrestFilterBuilder.() -> Unit
): Flow<Data> {
    val (key, initialData) = try {
        val result = supabaseClient.postgrest.from(schema, table).select {
            limit(1)
            single()
            filter(filter)
        }
        val data = result.decodeAs<Data>()
        primaryKey.producer(data) to data
    } catch (e: UnknownRestException) {
        error("Data matching filter and table name $table not found")
    }
    val changeFlow = postgresChangeFlow<PostgresAction>(schema) {
        this.table = table
        filter(primaryKey.columnName, FilterOperator.EQ, key)
    }
    return channelFlow {
        trySend(initialData)
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
    }
}

/**
 * This function retrieves the initial data for a single value and then listens for changes on that value. It automatically handles updates and closes the flow on the delete event.
 *
 * If you want more control, use the [postgresChangeFlow] function.
 * @param schema the schema of the table
 * @param table the table name
 * @param filter filter the the value you want to listen to
 * @param primaryKey the primary key of the [Data] type
 * @return a [Flow] of the current data. This flow emits a new value whenever a change occurs.
 */
suspend inline fun <reified Data, Value> RealtimeChannel.postgresSingleDataFlow(
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
