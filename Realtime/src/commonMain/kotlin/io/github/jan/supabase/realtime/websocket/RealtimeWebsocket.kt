package io.github.jan.supabase.realtime.websocket

import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.realtime.RealtimeMessage

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
     * Receive a message from the websocket.
     */
    suspend fun receive(): RealtimeMessage

    /**
     * Send a message to the websocket.
     */
    suspend fun send(message: RealtimeMessage)

    /**
     * Block the current coroutine until the websocket is disconnected.
     */
    suspend fun blockUntilDisconnect()

    /**
     * Disconnect the websocket.
     */
    fun disconnect()

}