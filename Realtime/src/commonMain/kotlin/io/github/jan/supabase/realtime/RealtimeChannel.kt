package io.github.jan.supabase.realtime

import co.touchlab.kermit.Logger
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.collections.AtomicMutableList
import io.github.jan.supabase.decode
import io.github.jan.supabase.decodeIfNotEmptyOrDefault
import io.github.jan.supabase.encodeToJsonElement
import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.putJsonObject
import io.github.jan.supabase.supabaseJson
import io.ktor.client.plugins.websocket.sendSerialized
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

/**
 * Represents a realtime channel
 */
sealed interface RealtimeChannel {

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

    @SupabaseInternal
    val callbackManager: CallbackManager

    /**
     * Joins the channel
     * @param blockUntilJoined if true, the method will block until the [status] is [Status.JOINED]
     */
    suspend fun join(blockUntilJoined: Boolean = false)

    /**
     * Updates the JWT token for this client
     */
    suspend fun updateAuth(jwt: String)

    /**
     * Leaves the channel
     */
    suspend fun leave()

    /**
     * Sends a message to everyone who joined the channel
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

    @SupabaseInternal
    fun RealtimeChannel.addPostgresChange(data: PostgresJoinConfig)

    /**
     * Represents the status of a channel
     */
    enum class Status {
        CLOSED,
        JOINING,
        JOINED,
        LEAVING,
    }

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

internal class RealtimeChannelImpl(
    private val realtimeImpl: RealtimeImpl,
    override val topic: String,
    private val broadcastJoinConfig: BroadcastJoinConfig,
    private val presenceJoinConfig: PresenceJoinConfig,
) : RealtimeChannel {

    private val clientChanges = AtomicMutableList<PostgresJoinConfig>()
    @SupabaseInternal
    override val callbackManager = CallbackManagerImpl(realtimeImpl)
    private val _status = MutableStateFlow(RealtimeChannel.Status.CLOSED)
    override val status = _status.asStateFlow()

    override val supabaseClient = realtimeImpl.supabaseClient

    @OptIn(SupabaseInternal::class)
    override suspend fun join(blockUntilJoined: Boolean) {
        if(realtimeImpl.status.value != Realtime.Status.CONNECTED) {
            error("Not connected to the realtime websocket. Try calling `supabaseClient.realtime.connect()` before attempting to join a channel.")
        }
        realtimeImpl.run {
            addChannel(this@RealtimeChannelImpl)
        }
        _status.value = RealtimeChannel.Status.JOINING
        Logger.d { "Joining channel $topic" }
        val currentJwt = realtimeImpl.config.jwtToken ?: supabaseClient.pluginManager.getPluginOrNull(GoTrue)?.currentAccessTokenOrNull()
        val postgrestChanges = clientChanges.toList()
        val joinConfig = RealtimeJoinPayload(RealtimeJoinConfig(broadcastJoinConfig, presenceJoinConfig, postgrestChanges))
        val joinConfigObject = buildJsonObject {
            putJsonObject(Json.encodeToJsonElement(joinConfig).jsonObject)
            currentJwt?.let {
                put("access_token", currentJwt)
            }
        }
        Logger.d { "Joining realtime socket with body $joinConfigObject" }
        realtimeImpl.ws?.sendSerialized(RealtimeMessage(topic, RealtimeChannel.CHANNEL_EVENT_JOIN, joinConfigObject, null))
        if(blockUntilJoined) {
            status.first { it == RealtimeChannel.Status.JOINED }
        }
    }

    @OptIn(SupabaseInternal::class)
    fun onMessage(message: RealtimeMessage) {
        if(message.eventType == null) {
            Logger.e { "Received message without event type: $message" }
            return
        }
        when(message.eventType) {
            RealtimeMessage.EventType.SYSTEM -> {
                Logger.d { "Joined channel ${message.topic}" }
                _status.value = RealtimeChannel.Status.JOINED
            }
            RealtimeMessage.EventType.POSTGRES_SERVER_CHANGES -> { //check if the server postgres_changes match with the client's and add the given id to the postgres change objects (to identify them later in the events)
                val serverPostgresChanges = message.payload["response"]?.jsonObject?.get("postgres_changes")?.jsonArray?.let { Json.decodeFromJsonElement<List<PostgresJoinConfig>>(it) } ?: listOf() //server postgres changes
                callbackManager.setServerChanges(serverPostgresChanges)
                if(status.value != RealtimeChannel.Status.JOINED) {
                    Logger.d { "Joined channel ${message.topic}" }
                    _status.value = RealtimeChannel.Status.JOINED
                }
            }
            RealtimeMessage.EventType.POSTGRES_CHANGES -> {
                val data = message.payload["data"]?.jsonObject ?: return
                val ids = message.payload["ids"]?.jsonArray?.mapNotNull { it.jsonPrimitive.longOrNull } ?: emptyList() //the ids of the matching postgres changes
                val postgresAction = supabaseJson.decodeFromJsonElement<PostgresActionData>(data)
                val action = when(data["type"]?.jsonPrimitive?.content ?: "") {
                    "UPDATE" -> PostgresAction.Update(postgresAction.record ?: error("Received no record on update event"), postgresAction.oldRecord ?: error("Received no old record on update event"), postgresAction.columns, postgresAction.commitTimestamp, realtimeImpl.serializer)
                    "DELETE" -> PostgresAction.Delete(postgresAction.oldRecord ?: error("Received no old record on delete event"), postgresAction.columns, postgresAction.commitTimestamp, realtimeImpl.serializer)
                    "INSERT" -> PostgresAction.Insert(postgresAction.record ?: error("Received no record on update event"), postgresAction.columns, postgresAction.commitTimestamp, realtimeImpl.serializer)
                    "SELECT" -> PostgresAction.Select(postgresAction.record ?: error("Received no record on update event"), postgresAction.columns, postgresAction.commitTimestamp, realtimeImpl.serializer)
                    else -> error("Unknown event type ${message.event}")
                }
                callbackManager.triggerPostgresChange(ids, action)
            }
            RealtimeMessage.EventType.BROADCAST -> {
                val event = message.payload["event"]?.jsonPrimitive?.content ?: ""
                callbackManager.triggerBroadcast(event, message.payload["payload"]?.jsonObject ?: JsonObject(mutableMapOf()))
            }
            RealtimeMessage.EventType.CLOSE -> {
                realtimeImpl.run {
                    deleteChannel(this@RealtimeChannelImpl)
                }
                Logger.d { "Left channel ${message.topic}" }
            }
            RealtimeMessage.EventType.ERROR -> {
                Logger.e { "Received an error in channel ${message.topic}. That could be as a result of an invalid access token" }
            }
            RealtimeMessage.EventType.PRESENCE_DIFF -> {
                val joins = message.payload["joins"]?.jsonObject?.decodeIfNotEmptyOrDefault(mapOf<String, Presence>()) ?: emptyMap()
                val leaves = message.payload["leaves"]?.jsonObject?.decodeIfNotEmptyOrDefault(mapOf<String, Presence>()) ?: emptyMap()
                callbackManager.triggerPresenceDiff(joins, leaves)
            }
            RealtimeMessage.EventType.PRESENCE_STATE -> {
                val joins = message.payload.decodeIfNotEmptyOrDefault(mapOf<String, Presence>())
                callbackManager.triggerPresenceDiff(joins, mapOf())
            }
        }
    }

    override suspend fun leave() {
        _status.value = RealtimeChannel.Status.LEAVING
        Logger.d { "Leaving channel $topic" }
        realtimeImpl.ws?.sendSerialized(RealtimeMessage(topic, RealtimeChannel.CHANNEL_EVENT_LEAVE, buildJsonObject {}, null))
    }

    override suspend fun updateAuth(jwt: String) {
        Logger.d { "Updating auth token for channel $topic" }
        realtimeImpl.ws?.sendSerialized(RealtimeMessage(topic, RealtimeChannel.CHANNEL_EVENT_ACCESS_TOKEN, buildJsonObject {
            put("access_token", jwt)
        }, (++realtimeImpl.ref).toString()))
    }

    override suspend fun broadcast(event: String, message: JsonObject) {
        if(status.value != RealtimeChannel.Status.JOINED) error("Cannot broadcast to a channel you didn't join. Did you forget to call join()?")
        realtimeImpl.ws?.sendSerialized(RealtimeMessage(topic, "broadcast", buildJsonObject {
            put("type", "broadcast")
            put("event", event)
            put("payload", message)
        }, (++realtimeImpl.ref).toString()))
    }

    @SupabaseInternal
    override fun RealtimeChannel.addPostgresChange(data: PostgresJoinConfig) {
        clientChanges.add(data)
    }

    override suspend fun track(state: JsonObject) {
        val payload = buildJsonObject {
            put("type", "presence")
            put("event", "track")
            putJsonObject("payload") {
                putJsonObject(state)
            }
        }
        realtimeImpl.ws?.sendSerialized(RealtimeMessage(topic, RealtimeChannel.CHANNEL_EVENT_PRESENCE, payload, (++realtimeImpl.ref).toString()))
    }

    override suspend fun untrack() {
        realtimeImpl.ws?.sendSerialized(RealtimeMessage(topic, RealtimeChannel.CHANNEL_EVENT_PRESENCE, buildJsonObject {
            put("type", "presence")
            put("event", "untrack")
        }, (++realtimeImpl.ref).toString()))
    }

}

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
@OptIn(SupabaseInternal::class)
fun RealtimeChannel.presenceChangeFlow(): Flow<PresenceAction> {
    return callbackFlow {
        val callback: (PresenceAction) -> Unit = { action ->
            trySend(action)
        }

        val id = callbackManager.addPresenceCallback(callback)
        awaitClose { callbackManager.removeCallbackById(id) }
    }
}

/**
 * You can listen for postgres changes in a channel.
 *
 * Example:
 * ```kotlin
 * val productChangeFlow = channel.postgrestChangeFlow<PostgresAction.Update>("public") {
 *    table = "products"
 * }.map { it.decodeRecord<Product>() }
 * ```
 *
 * @param T The event type you want to listen to (e.g. [PostgresAction.Update] for updates or only [PostgresAction] for all)
 * @param schema The schema name of the table that is being monitored. For normal supabase tables that might be "public".
 */
@OptIn(SupabaseInternal::class)
inline fun <reified T : PostgresAction> RealtimeChannel.postgresChangeFlow(schema: String, filter: PostgresChangeFilter.() -> Unit = {}): Flow<T> {
    if(status.value == RealtimeChannel.Status.JOINED) error("You cannot call postgresChangeFlow after joining the channel")
    val event = when(T::class) {
        PostgresAction.Insert::class -> "INSERT"
        PostgresAction.Update::class -> "UPDATE"
        PostgresAction.Delete::class -> "DELETE"
        PostgresAction.Select::class -> "SELECT"
        PostgresAction::class -> "*"
        else -> error("Unknown event type ${T::class}")
    }
    val postgrestBuilder = PostgresChangeFilter(event, schema).apply(filter)
    val config = postgrestBuilder.buildConfig()
    addPostgresChange(config)
    return callbackFlow {
        val callback: (PostgresAction) -> Unit = {
            if (it is T) {
                trySend(it)
            }
        }

        val id = callbackManager.addPostgresCallback(config, callback)
        awaitClose { callbackManager.removeCallbackById(id) }
    }
}

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
inline fun <reified T : Any> RealtimeChannel.broadcastFlow(event: String): Flow<T> = callbackFlow {
    val id = callbackManager.addBroadcastCallback(event) {
        val decodedValue = try {
            supabaseClient.realtime.serializer.decode<T>(it.toString())
        } catch(e: Exception) {
            Logger.e(e) { "Couldn't decode $this as ${T::class.simpleName}. The corresponding handler wasn't called" }
            null
        }
        decodedValue?.let { value -> trySend(value) }
    }
    awaitClose { callbackManager.removeCallbackById(id) }
}

/**
 * Sends a message to everyone who joined the channel
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