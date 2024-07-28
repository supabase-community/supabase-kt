package io.github.jan.supabase.realtime.websocket

import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.webSocketSession

class KtorRealtimeWebsocketFactory(
    private val httpClient: HttpClient,
    ): RealtimeWebsocketFactory {

    override suspend fun create(url: String): RealtimeWebsocket {
        val ws = httpClient.webSocketSession(url)
        return KtorRealtimeWebsocket(ws)
    }

}