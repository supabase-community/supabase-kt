package io.github.jan.supabase.realtime

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.logging.SupabaseLogger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.JsonObject
import kotlin.reflect.KClass

/**
 * Represents a realtime channel
 */
interface RealtimeChannel {

    /**
     * The status of the channel as a [StateFlow]
     */
    val status: StateFlow<Status>

    /**
     * The topic of the channel
     */
    val topic: String

    /**
     * The current [SupabaseClient]
     */
    val supabaseClient: SupabaseClient

    /**
     * The realtime instance
     */
    val realtime: Realtime

    @SupabaseInternal
    val callbackManager: CallbackManager

    @SupabaseInternal
    val logger: SupabaseLogger

    /**
     * Subscribes to the channel
     * @param blockUntilSubscribed if true, the method will block the coroutine until the [status] is [Status.SUBSCRIBED]
     */
    suspend fun subscribe(blockUntilSubscribed: Boolean = false)

    /**
     * Updates the JWT token for this channel
     */
    suspend fun updateAuth(jwt: String?)

    /**
     * Unsubscribes from the channel
     */
    suspend fun unsubscribe()

    /**
     * Sends a message to everyone who joined the channel. Can be used even if you aren't connected to the channel.
     * @param event the broadcast event. Example: mouse_cursor
     * @param message the message to send as a JsonObject
     */
    suspend fun broadcast(event: String, message: JsonObject)

    suspend fun broadcast(event: String, data: ByteArray)

    /**
     * Sends a broadcast message explicitly via REST API.
     *
     * This method always uses the REST API endpoint regardless of WebSocket connection state.
     * Useful when you want to guarantee REST delivery or when gradually migrating from implicit REST fallback.
     *
     * Payloads that are a [ByteArray] are sent as
     * `application/octet-stream`; all other payloads are JSON-encoded.
     * @param event The name of the broadcast event
     * @param payload Payload to be sent (required)
     * @param builder Options including timeout
     * @throws RestException If any error appears after sending the payload
     * @throws IllegalStateException If the realtime version does not support sending broadcasts via HTTP
     *
     */
    suspend fun httpSend(event: String, payload: HttpSendPayload, builder: HttpSendBuilder.() -> Unit = {})

    /**
     * Store an object in your presence's state. Other clients can get this data when you either join or leave the channel.
     * Use this method again to update the state.
     * @param state the data to store
     */
    suspend fun track(state: JsonObject)

    /**
     * Removes the object from your presence's state
     */
    suspend fun untrack()

    /**
     * Non-inline variant of [postgresChangeFlow] for implementation and mocking purposes
     */
    @SupabaseInternal
    fun <T : PostgresAction> RealtimeChannel.postgresChangeFlowInternal(action: KClass<T>, schema: String, filter: PostgresChangeFilter.() -> Unit = {}): Flow<T>

    /**
     * Non-inline variant of [broadcastFlow] for implementation and mocking purposes
     */
    fun RealtimeChannel.broadcastFlow(event: String): Flow<RealtimeBroadcast<*>>

    /**
     * Listen for clients joining / leaving the channel using presences
     *
     * Example:
     * ```kotlin
     * val presenceChangeFlow = channel.presenceChangeFlow()
     *
     * presenceChangeFlow.collect {
     *    val joins = it.decodeJoinsAs<User>()
     *    val leaves = it.decodeLeavesAs<User>()
     * }
     * ```
     */
    fun presenceChangeFlow(): Flow<PresenceAction>

    @SupabaseInternal
    fun RealtimeChannel.addPostgresChange(data: PostgresJoinConfig)

    @SupabaseInternal
    fun RealtimeChannel.removePostgresChange(data: PostgresJoinConfig)

    @SupabaseInternal
    fun updateStatus(status: Status)

    @SupabaseInternal
    suspend fun scheduleRejoin()

    @SupabaseInternal
    fun teardown()

    /**
     * Represents the status of a channel
     */
    enum class Status {
        /**
         * The [RealtimeChannel] is currently unsubscribed
         */
        UNSUBSCRIBED,

        /**
         * The [RealtimeChannel] is currently in the process of subscribing
         */
        SUBSCRIBING,

        /**
         * The [RealtimeChannel] is subscribed
         */
        SUBSCRIBED,

        /**
         * The [RealtimeChannel] is in the process of unsubscribing
         */
        UNSUBSCRIBING,
    }

    /**
     * @see RealtimeChannel
     */
    @Suppress("UndocumentedPublicProperty")
    companion object {
        const val CHANNEL_EVENT_JOIN = "phx_join"
        const val CHANNEL_EVENT_LEAVE = "phx_leave"
        const val CHANNEL_EVENT_CLOSE = "phx_close"
        const val CHANNEL_EVENT_ERROR = "phx_error"
        const val CHANNEL_EVENT_REPLY = "phx_reply"
        const val CHANNEL_EVENT_SYSTEM = "system"
        const val CHANNEL_EVENT_BROADCAST = "broadcast"
        const val CHANNEL_EVENT_ACCESS_TOKEN = "access_token"
        const val CHANNEL_EVENT_PRESENCE = "presence"
        const val CHANNEL_EVENT_PRESENCE_DIFF = "presence_diff"
        const val CHANNEL_EVENT_PRESENCE_STATE = "presence_state"
        const val CHANNEL_EVENT_POSTGRES_CHANGES = "postgres_changes"
    }

}