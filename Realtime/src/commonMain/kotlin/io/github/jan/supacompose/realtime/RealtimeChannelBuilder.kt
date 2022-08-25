package io.github.jan.supacompose.realtime

import io.github.aakira.napier.Napier
import io.github.jan.supacompose.realtime.annotiations.ChannelDsl
import io.github.jan.supacompose.realtime.events.receiver.PostgresReceiver
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

    inline fun onPostgrestChange(builder: PostgresReceiver.() -> Unit) {
        val receiver = PostgresReceiver().apply(builder)
        bindings["postgres_changes"] = bindings.getOrElse("postgres_changes") { emptyList() } + receiver.toBinding()
    }

    inline fun <reified T> onBroadcast(event: String, json: Json = Json, crossinline handler: T.() -> Unit) {
        bindings["broadcast"] = bindings.getOrElse("broadcast") { emptyList() } + RealtimeBinding.DefaultRealtimeBinding(event) {
            val decodedValue = try {
                json.decodeFromString<T>(this.toString())
            } catch(e: Exception) {
                Napier.e(e) { "Couldn't decode $this as ${T::class.simpleName}. The corresponding handler wasn't called" }
                null
            }
            decodedValue?.let { handler(it) }
        }
    }

    private fun buildJoinPayload(): RealtimeJoinPayload {
        return RealtimeJoinPayload(
            RealtimeJoinConfig(BroadcastJoinConfig(false, false), PresenceJoinConfig(""), listOf()),
        )
    }

    fun build(): RealtimeChannel {
        return RealtimeChannelImpl(
            realtimeImpl,
            topic,
            bindings,
            "",
            mutableListOf()
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