package io.github.jan.supacompose.realtime

import io.github.jan.supacompose.auth.auth
import io.github.jan.supacompose.realtime.events.ChannelAction
import io.github.jan.supacompose.realtime.events.EventListener

@ChannelDsl
class RealtimeChannelBuilder @PublishedApi internal constructor(private val realtimeImpl: RealtimeImpl) {

    var schema = ""
    var table = ""
    var column: String? = null
    var value: String? = null
    val eventListener = mutableListOf<EventListener>()

    @ChannelDsl
    inline fun <reified Action: ChannelAction> on(crossinline listener: (@ChannelDsl Action).() -> Unit) {
       eventListener.add {
           if(it is Action) {
               listener(it)
           }
       }
    }

    @ChannelDsl
    inline fun onAll(crossinline listener: (@ChannelDsl ChannelAction).() -> Unit) {
        eventListener.add {
            listener(it)
        }
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
            eventListener
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