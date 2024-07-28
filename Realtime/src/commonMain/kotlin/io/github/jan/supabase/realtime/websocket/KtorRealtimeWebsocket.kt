package io.github.jan.supabase.realtime.websocket

import io.github.jan.supabase.realtime.RealtimeMessage
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.receiveDeserialized
import io.ktor.client.plugins.websocket.sendSerialized
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.job

class KtorRealtimeWebsocket(
    private val ws: DefaultClientWebSocketSession
): RealtimeWebsocket {

    override val hasIncomingMessages: Boolean get() = ws.isActive

    override suspend fun receive(): RealtimeMessage {
        return ws.receiveDeserialized()
    }

    override suspend fun send(message: RealtimeMessage) {
        ws.sendSerialized(message)
    }

    override fun disconnect() {
        ws.cancel()
    }

    override suspend fun blockUntilDisconnect() {
        ws.coroutineContext.job.join()
    }

}