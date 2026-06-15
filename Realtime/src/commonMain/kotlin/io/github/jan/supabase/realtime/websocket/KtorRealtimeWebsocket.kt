package io.github.jan.supabase.realtime.websocket

import io.github.jan.supabase.realtime.RealtimeMessage
import io.github.jan.supabase.realtime.RealtimeProtocolVersion
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.sendSerialized
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.job

/**
 * Implementation of [RealtimeWebsocket] using Ktor's [DefaultClientWebSocketSession].
 */
class KtorRealtimeWebsocket(
    private val ws: DefaultClientWebSocketSession,
    val vsn: RealtimeProtocolVersion
): RealtimeWebsocket {

    override val hasIncomingMessages: Boolean get() = ws.isActive

    override suspend fun receive(): RealtimeFrame {
        TODO("")
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