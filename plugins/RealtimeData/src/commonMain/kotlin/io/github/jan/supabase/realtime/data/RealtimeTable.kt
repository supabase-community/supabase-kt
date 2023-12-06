package io.github.jan.supabase.realtime.data

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.collections.AtomicMutableMap
import io.github.jan.supabase.exceptions.NotFoundRestException
import io.github.jan.supabase.exceptions.UnknownRestException
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.filter.PostgrestFilterBuilder
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

class RealtimeTable <Data> (
    val table: String,
    val schema: String,
    val supabaseClient: SupabaseClient,
    val decodeData: (String) -> Data,
    val decodeDataList: (String) -> List<Data>,
) {

    inline fun listFlow(
        filter: String = "",
        crossinline primaryKey: (Data) -> String,
    ): Flow<List<Data>> = callbackFlow {
        val cache = AtomicMutableMap<String, Data>()
        try {
            val result = supabaseClient.postgrest.from(schema, table).select()
            val data = decodeDataList(result.data)
            data.forEach {
                val key = primaryKey(it)
                cache[key] = it
            }
            trySend(decodeDataList(result.data))
        } catch (e: NotFoundRestException) {
            close(IllegalStateException("Table with name $table not found"))
            return@callbackFlow
        }
        val channel = supabaseClient.realtime.channel("$table$schema")
        val changeFlow = channel.postgresChangeFlow<PostgresAction>(schema) {
            this.table = this@RealtimeTable.table
            if(filter.isNotBlank()) {
                this.filter = filter
            }
        }
        launch {
            changeFlow.collect {
                when (it) {
                    is PostgresAction.Insert -> {
                        val data = decodeData(it.record.toString())
                        val key = primaryKey(data)
                        cache[key] = data
                    }
                    is PostgresAction.Update -> {
                        val data = decodeData(it.record.toString())
                        val key = primaryKey(data)
                        cache[key] = data
                    }
                    is PostgresAction.Delete -> {
                        val data = decodeData(it.oldRecord.toString())
                        val key = primaryKey(data)
                        cache.remove(key)
                    }
                    else -> {}
                }
                trySend(cache.values.toList())
            }
        }
        channel.subscribe()
        awaitClose {
            launch {
                supabaseClient.realtime.removeChannel(channel)
            }
        }
    }

    inline fun dataFlow(
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
        val channel = supabaseClient.realtime.channel("$table$schema")
        val changeFlow = channel.postgresChangeFlow<PostgresAction>(schema) {
            this.table = this@RealtimeTable.table
            this.filter = "${key.columnName}=eq.${key.value}"
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
                        supabaseClient.realtime.removeChannel(channel)
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


}