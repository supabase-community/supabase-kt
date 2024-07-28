package io.github.jan.supabase.realtime.websocket

interface RealtimeWebsocketFactory {

    suspend fun create(url: String): RealtimeWebsocket

}