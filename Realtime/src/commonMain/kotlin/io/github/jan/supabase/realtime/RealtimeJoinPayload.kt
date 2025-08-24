package io.github.jan.supabase.realtime

import io.github.jan.supabase.annotations.SupabaseInternal
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@SupabaseInternal
@Serializable
data class RealtimeJoinPayload(
    val config: RealtimeJoinConfig
)

@SupabaseInternal
@Serializable
data class RealtimeJoinConfig(
    val broadcast: BroadcastJoinConfig,
    val presence: PresenceJoinConfig,
    @SerialName("postgres_changes")
    val postgresChanges: List<PostgresJoinConfig>,
    @SerialName("private")
    var isPrivate: Boolean
)

/**
 * @param acknowledgeBroadcasts Whether the server should send an acknowledgment message for each broadcast message
 * @param receiveOwnBroadcasts Whether you should receive your own broadcasts
 */
@Serializable
data class BroadcastJoinConfig(@SerialName("ack") var acknowledgeBroadcasts: Boolean, @SerialName("self") var receiveOwnBroadcasts: Boolean)

/**
 * @param key Used to track presence payloads. Can be e.g. a user id
 * @param enabled Whether presence is enabled for this channel
 */
@Serializable
data class PresenceJoinConfig(var key: String, internal var enabled: Boolean)

@SupabaseInternal
@Serializable
data class PostgresJoinConfig(val schema: String, val table: String? = null, val filter: String? = null, val event: String, val id: Int = 0) {

    override fun equals(other: Any?): Boolean {
        if(other !is PostgresJoinConfig) return false
        return other.schema == schema && other.table == table && other.filter == filter && (other.event == event || other.event == "*")
    }

    override fun hashCode(): Int {
        var result = schema.hashCode()
        result = 31 * result + (table?.hashCode() ?: 0)
        result = 31 * result + (filter?.hashCode() ?: 0)
        result = 31 * result + event.hashCode()
        return result
    }

}
