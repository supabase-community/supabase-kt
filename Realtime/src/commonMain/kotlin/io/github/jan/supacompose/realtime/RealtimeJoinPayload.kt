package io.github.jan.supacompose.realtime

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RealtimeJoinPayload(
    val config: RealtimeJoinConfig,
    @SerialName("access_token")
    val accessToken: String = ""
)

@Serializable
data class RealtimeJoinConfig(
    val broadcast: BroadcastJoinConfig,
    val presence: PresenceJoinConfig,
    @SerialName("postgres_changes")
    val postgrestChanges: List<PostgresJoinConfig>
)

@Serializable
data class BroadcastJoinConfig(val ack: Boolean, val self: Boolean)

@Serializable
data class PresenceJoinConfig(val key: String)

@Serializable
data class PostgresJoinConfig(val schema: String, val table: String? = null, val filter: String? = null, val event: String, val id: Long = 0L)
