package io.github.jan.supacompose.realtime

import io.github.aakira.napier.Napier
import io.github.jan.supacompose.realtime.annotiations.ChannelDsl
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

sealed interface RealtimeBinding {

    val filter: Any
    val callback: Any.() -> Unit

    data class PostgrestRealtimeBinding(override val filter: PostgresJoinConfig, override val callback: Any.() -> Unit) : RealtimeBinding

    data class DefaultRealtimeBinding(override val filter: String, override val callback: Any.() -> Unit) : RealtimeBinding

}

@ChannelDsl
class RealtimeChannelBuilder @PublishedApi internal constructor(private val topic: String, private val realtimeImpl: RealtimeImpl) {

    val bindings = mutableMapOf<String, List<RealtimeBinding>>()

    //broadcast config

    //presence config

    //other presence related stuff

    fun build(): RealtimeChannel {
        return RealtimeChannelImpl(
            realtimeImpl,
            topic,
            bindings,
            ""
        )
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun generateKey(schema: String, table: String?, column: String?, value: String?): String {
        if (value != null && (column == null || table == null)) throw IllegalStateException("When using a value, you need to specify a table and a column")
        if (column != null && table == null) throw IllegalStateException("When using a column, you need to specify a table")
        return buildString {
            append(listOfNotNull("realtime", schema, table).joinToString(":").trim())
            column?.let {
                append(it)
                value?.let {
                    append("=eq.$value")
                }
            }
        }
    }

}