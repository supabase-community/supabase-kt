package io.github.jan.supabase.realtime.websocket

import io.github.jan.supabase.realtime.RealtimeMessage

interface RealtimeWebsocket {

    val hasIncomingMessages: Boolean

    suspend fun receive(): RealtimeMessage

    suspend fun send(message: RealtimeMessage)

    suspend fun blockUntilDisconnect()

    fun disconnect()

}