package io.github.jan.supabase.realtime.websocket

import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.realtime.RealtimeMessage
import io.ktor.websocket.Frame

/**
 * Interface for a websocket connection to the Supabase Realtime service.
 */
@OptIn(ExperimentalSubclassOptIn::class)
@SubclassOptInRequired(SupabaseInternal::class)
interface RealtimeWebsocket {

    /**
     * Whether there are incoming messages that can be received.
     */
    val hasIncomingMessages: Boolean

    /**
     * Receive a frame from the websocket.
     */
    suspend fun receive(): Frame

    /**
     * Send a message to the websocket.
     */
    suspend fun send(message: RealtimeMessage)

    suspend fun send(data: ByteArray)

    /**
     * Block the current coroutine until the websocket is disconnected.
     */
    suspend fun blockUntilDisconnect()

    /**
     * Disconnect the websocket.
     */
    fun disconnect()

}