package io.github.jan.supacompose.realtime

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
@Serializable
data class RealtimeMessage(val topic: String, val event: String, val payload: JsonObject, val ref: String?)
