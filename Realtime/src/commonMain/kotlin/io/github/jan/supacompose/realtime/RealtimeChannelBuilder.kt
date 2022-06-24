package io.github.jan.supacompose.realtime

import io.github.jan.supacompose.auth.auth

@ChannelDsl
class RealtimeChannelBuilder @PublishedApi internal constructor(private val realtimeImpl: RealtimeImpl) {

    var schema = ""
    var table = ""
    var column: String? = null
    var value: String? = null
    private val listeners = mutableListOf<Pair<ChannelAction, (RealtimeChannelMessage) -> Unit>>()

    @ChannelDsl
    fun on(action: ChannelAction, listener: (RealtimeChannelMessage) -> Unit) {
        listeners.add(action to listener)
    }

    fun build(): RealtimeChannel {
        val key = generateKey(schema, table, column, value)
        return RealtimeChannelImpl(
            realtimeImpl,
            key,
            schema,
            table,
            column,
            value,
            realtimeImpl.supabaseClient.auth.currentSession.value?.accessToken
                ?: throw IllegalStateException("You can't join a channel without an user session"),
            listeners
        )
    }

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