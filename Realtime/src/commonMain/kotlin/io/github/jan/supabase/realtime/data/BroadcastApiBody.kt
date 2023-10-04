package io.github.jan.supabase.realtime.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
internal data class BroadcastApiBody(
    val messages: List<BroadcastApiMessage>
)

@Serializable
internal data class BroadcastApiMessage(
    val topic: String,
    val event: String,
    val payload: JsonObject
)
