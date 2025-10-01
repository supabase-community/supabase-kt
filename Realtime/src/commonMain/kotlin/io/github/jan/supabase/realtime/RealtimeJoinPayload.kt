@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)

package io.github.jan.supabase.realtime

import io.github.jan.supabase.annotations.SupabaseInternal
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
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
    val postgrestChanges: List<PostgresJoinConfig>
)

/**
 * @param acknowledgeBroadcasts Whether the server should send an acknowledgment message for each broadcast message
 * @param receiveOwnBroadcasts Whether you should receive your own broadcasts
 */
@Serializable
data class BroadcastJoinConfig(@SerialName("ack") var acknowledgeBroadcasts: Boolean, @SerialName("self") var receiveOwnBroadcasts: Boolean)

/**
 * @param key Used to track presence payloads. Can be e.g. a user id
 */
@Serializable
data class PresenceJoinConfig(
    var key: String,
    @EncodeDefault
    var enabled: Boolean = false,
)

@SupabaseInternal
@Serializable
data class PostgresJoinConfig(val schema: String, val table: String? = null, val filter: String? = null, val event: String, val id: Long = 0L) {

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
