package io.github.jan.supabase.realtime

import kotlinx.serialization.json.JsonElement

/**
 * Represents the payload used in [RealtimeChannel.httpSend]
 */
sealed interface HttpSendPayload {

    /**
     * JSON-encoded payload via [value]
     */
    data class Json(val value: JsonElement): HttpSendPayload

    /**
     * Binary payload via [buffer]
     */
    class Binary(val buffer: ByteArray): HttpSendPayload

}
