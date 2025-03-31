package io.github.jan.supabase.realtime

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.encodeToJsonElement
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf

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
    @SupabaseInternal
    fun <T : Any> RealtimeChannel.broadcastFlowInternal(type: KType, event: String): Flow<T>

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

/**
 * Listen for postgres changes in a channel.
 *
 * Example:
 * ```kotlin
 * val productChangeFlow = channel.postgrestChangeFlow<PostgresAction.Update>("public") {
 *    table = "products"
 * }.map { it.decodeRecord<Product>() }
 * ```
 *
 * If you just want to check for changes and also retrieve initial values, you can use the [postgresListDataFlow] or [postgresSingleDataFlow] functions.
 *
 * @param T The event type you want to listen to (e.g. [PostgresAction.Update] for updates or only [PostgresAction] for all)
 * @param schema The schema name of the table that is being monitored. For normal supabase tables that might be "public".
 */
inline fun <reified T : PostgresAction> RealtimeChannel.postgresChangeFlow(
    schema: String,
    noinline filter: PostgresChangeFilter.() -> Unit = {}
): Flow<T> = postgresChangeFlowInternal(T::class, schema, filter)

/**
 * Broadcasts can be messages sent by other clients within the same channel under a specific [event].
 *
 * Example:
 * ```kotlin
 * val messageFlow = channel.broadcastFlow<Message>("message")
 * messageFlow.collect {
 *    println("Received message: $it")
 * }
 * ```
 *
 * @param event When a message is sent by another client, it will be sent under a specific event. This is the event that you want to listen to
 */
@OptIn(SupabaseInternal::class)
inline fun <reified T : Any> RealtimeChannel.broadcastFlow(event: String): Flow<T> = broadcastFlowInternal(typeOf<T>(), event)

/**
 * Sends a message to everyone who joined the channel. Can be used even if you aren't connected to the channel.
 * @param event the broadcast event. Example: mouse_cursor
 * @param message the message to send as [T] (can only be something that can be encoded as a json object)
 */
suspend inline fun <reified T : Any> RealtimeChannel.broadcast(event: String, message: T) = broadcast(event, supabaseClient.realtime.serializer.encodeToJsonElement(message).jsonObject)

/**
 * Store an object in your presence's state. Other clients can get this data when you either join or leave the channel.
 * Use this method again to update the state.
 *
 * Example:
 * ```kotlin
 * @Serializable
 * data class PresenceData(val name: String)
 *
 * channel.track(PresenceData("Your Name"))
 * ```
 *
 * @param state the data to store (can only be something that can be encoded as a json object)
 */
suspend inline fun <reified T : Any> RealtimeChannel.track(state: T) = track(supabaseClient.realtime.serializer.encodeToJsonElement(state).jsonObject)