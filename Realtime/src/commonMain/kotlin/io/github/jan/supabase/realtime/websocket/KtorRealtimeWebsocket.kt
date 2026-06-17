package io.github.jan.supabase.realtime.websocket

import io.github.jan.supabase.realtime.RealtimeMessage
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.sendSerialized
import io.ktor.websocket.Frame
import io.ktor.websocket.send
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.job

/**
 * Implementation of [RealtimeWebsocket] using Ktor's [DefaultClientWebSocketSession].
 */
class KtorRealtimeWebsocket(
    private val ws: DefaultClientWebSocketSession,
): RealtimeWebsocket {

    override val hasIncomingMessages: Boolean get() = ws.isActive

    override suspend fun receive(): Frame {
        return ws.incoming.receive()
    }


    override suspend fun send(message: RealtimeMessage) {
        ws.sendSerialized(message)
    }

    override suspend fun send(data: ByteArray) {
        ws.send(data)
    }

    override fun disconnect() {
        ws.cancel()
    }

    override suspend fun blockUntilDisconnect() {
        ws.coroutineContext.job.join()
    }

}