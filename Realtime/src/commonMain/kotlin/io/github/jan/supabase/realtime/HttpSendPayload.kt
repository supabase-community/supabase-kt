package io.github.jan.supabase.realtime

import kotlinx.serialization.json.JsonElement

sealed interface HttpSendPayload {

    data class Json(val value: JsonElement): HttpSendPayload

    class Binary(val buffer: ByteArray): HttpSendPayload

}
