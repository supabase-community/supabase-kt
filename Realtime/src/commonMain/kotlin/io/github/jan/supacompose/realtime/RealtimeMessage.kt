package io.github.jan.supacompose.realtime

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * Represents a message retrieved by the [RealtimeChannel]
 */
@Serializable
data class RealtimeMessage(val topic: String, val event: String, val payload: JsonObject, val ref: String?)
