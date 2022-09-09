package io.github.jan.supacompose.realtime

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RealtimeJoinPayload(
    val config: RealtimeJoinConfig
)

@Serializable
data class RealtimeJoinConfig(
    val broadcast: BroadcastJoinConfig,
    val presence: PresenceJoinConfig,
    @SerialName("postgres_changes")
    val postgrestChanges: List<PostgresJoinConfig>
)

/**
 * @param acknowledgeBroadcasts Whether the server should send an acknowledgment message for each broadcast message
 * @param receiveOwnBroadcasts Whether you should receive your own broadcasts
 */
@Serializable
data class BroadcastJoinConfig(@SerialName("ack") var acknowledgeBroadcasts: Boolean = false, @SerialName("self") var receiveOwnBroadcasts: Boolean = false)

/**
 * @param key Used to track presence payloads
 */
@Serializable
data class PresenceJoinConfig(var key: String = "")

@Serializable
data class PostgresJoinConfig(val schema: String, val table: String? = null, val filter: String? = null, val event: String, val id: Long = 0L)
