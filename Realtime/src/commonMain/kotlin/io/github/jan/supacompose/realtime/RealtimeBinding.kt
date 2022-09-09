package io.github.jan.supacompose.realtime

sealed interface RealtimeBinding {

    val filter: Any
    val callback: Any.() -> Unit

    data class PostgrestRealtimeBinding(override val filter: PostgresJoinConfig, override val callback: Any.() -> Unit) : RealtimeBinding

    data class DefaultRealtimeBinding(override val filter: String, override val callback: Any.() -> Unit) : RealtimeBinding

}