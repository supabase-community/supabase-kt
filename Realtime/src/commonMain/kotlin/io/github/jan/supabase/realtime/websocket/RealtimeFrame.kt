package io.github.jan.supabase.realtime.websocket

sealed interface RealtimeFrame {

    class Binary(val data: ByteArray)

    data class Text(val text: String)

}