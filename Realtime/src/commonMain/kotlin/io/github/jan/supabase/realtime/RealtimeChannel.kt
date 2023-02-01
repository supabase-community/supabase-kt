package io.github.jan.supabase.realtime

import io.github.aakira.napier.Napier
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotiations.SupabaseInternal
import io.github.jan.supabase.decodeIfNotEmptyOrDefault
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

/**
 * Represents a realtime channel
 */
sealed interface RealtimeChannel {

    val status: StateFlow<Status>
    val topic: String
    val supabaseClient: SupabaseClient

    @SupabaseInternal
    val callbackManager: CallbackManager

    /**
     * Joins the channel
     */
    suspend fun join()

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

    enum class Status {
        CLOSED,
        JOINING,
        JOINED,
        LEAVING,
    }

    companion object {
        const val CHANNEL_EVENT_JOIN = "phx_join"
        const val CHANNEL_EVENT_LEAVE = "phx_leave"
        const val CHANNEL_EVENT_CLOSE = "phx_close"
        const val CHANNEL_EVENT_ERROR = "phx_error"
        const val CHANNEL_EVENT_REPLY = "phx_reply"
        const val CHANNEL_EVENT_BROADCAST = "broadcast"
        const val CHANNEL_EVENT_ACCESS_TOKEN = "access_token"
        const val CHANNEL_EVENT_PRESENCE = "presence"
        const val CHANNEL_EVENT_PRESENCE_DIFF = "presence_diff"
        const val CHANNEL_EVENT_PRESENCE_STATE = "presence_state"
    }

}

internal class RealtimeChannelImpl(
    private val realtimeImpl: RealtimeImpl,
    override val topic: String,
    private val broadcastJoinConfig: BroadcastJoinConfig,
    private val presenceJoinConfig: PresenceJoinConfig,
) : RealtimeChannel {

    private val clientChanges = mutableListOf<PostgresJoinConfig>()
    @SupabaseInternal
    override val callbackManager = CallbackManagerImpl()
    private val _status = MutableStateFlow(RealtimeChannel.Status.CLOSED)
    override val status = _status.asStateFlow()

    override val supabaseClient = realtimeImpl.supabaseClient

    @OptIn(SupabaseInternal::class)
    override suspend fun join() {
        realtimeImpl.run {
            addChannel(this@RealtimeChannelImpl)
        }
        _status.value = RealtimeChannel.Status.JOINING
        Napier.d { "Joining channel $topic" }
        val currentJwt = realtimeImpl.config.jwtToken ?: supabaseClient.pluginManager.getPluginOrNull(GoTrue)?.currentAccessTokenOrNull()
        val postgrestChanges = clientChanges.toList()
        val joinConfig = RealtimeJoinPayload(RealtimeJoinConfig(broadcastJoinConfig, presenceJoinConfig, postgrestChanges))
        val joinConfigObject = buildJsonObject {
            putJsonObject(Json.encodeToJsonElement(joinConfig).jsonObject)
            currentJwt?.let {
                put("access_token", currentJwt)
            }
        }
        Napier.d { "Joining realtime socket with body $joinConfigObject" }
        realtimeImpl.ws?.sendSerialized(RealtimeMessage(topic, RealtimeChannel.CHANNEL_EVENT_JOIN, joinConfigObject, null))
    }

    @OptIn(SupabaseInternal::class)
    fun onMessage(message: RealtimeMessage) {
        when {
            message.event == "system" && message.payload["status"]?.jsonPrimitive?.content == "ok" -> {
                Napier.d { "Joined channel ${message.topic}" }
                _status.value = RealtimeChannel.Status.JOINED
            }
            message.event == RealtimeChannel.CHANNEL_EVENT_REPLY && message.payload["response"]?.jsonObject?.containsKey("postgres_changes") ?: false -> { //check if the server postgres_changes match with the client's and add the given id to the postgres change objects (to identify them later in the events)
                val serverPostgresChanges = message.payload["response"]?.jsonObject?.get("postgres_changes")?.jsonArray?.let { Json.decodeFromJsonElement<List<PostgresJoinConfig>>(it) } ?: listOf() //server postgres changes
                callbackManager.serverChanges = serverPostgresChanges
            }
            message.event == "postgres_changes" -> {
                val data = message.payload["data"]?.jsonObject ?: return
                val ids = message.payload["ids"]?.jsonArray?.mapNotNull { it.jsonPrimitive.longOrNull } ?: emptyList() //the ids of the matching postgres changes
                val action = when(data["type"]?.jsonPrimitive?.content ?: "") {
                    "UPDATE" -> supabaseJson.decodeFromJsonElement<PostgresAction.Update>(data)
                    "DELETE" -> supabaseJson.decodeFromJsonElement<PostgresAction.Delete>(data)
                    "INSERT" -> supabaseJson.decodeFromJsonElement<PostgresAction.Insert>(data)
                    "SELECT" -> supabaseJson.decodeFromJsonElement<PostgresAction.Select>(data)
                    else -> throw IllegalStateException("Unknown event type ${message.event}")
                }
                callbackManager.triggerPostgresChange(ids, action)
            }
            message.event == RealtimeChannel.CHANNEL_EVENT_BROADCAST -> {
                val event = message.payload["event"]?.jsonPrimitive?.content ?: ""
                callbackManager.triggerBroadcast(event, message.payload["payload"]?.jsonObject ?: JsonObject(mutableMapOf()))
            }
            message.event == RealtimeChannel.CHANNEL_EVENT_CLOSE -> {
                realtimeImpl.run {
                    removeChannel(topic)
                }
                Napier.d { "Left channel ${message.topic}" }
            }
            message.event == RealtimeChannel.CHANNEL_EVENT_ERROR -> {
                Napier.e { "Received an error in channel ${message.topic}. That could be as a result of an invalid access token" }
            }
            message.event == RealtimeChannel.CHANNEL_EVENT_PRESENCE_DIFF -> {
                val joins = message.payload["joins"]?.jsonObject?.decodeIfNotEmptyOrDefault(mapOf<String, Presence>()) ?: emptyMap()
                val leaves = message.payload["leaves"]?.jsonObject?.decodeIfNotEmptyOrDefault(mapOf<String, Presence>()) ?: emptyMap()
                callbackManager.triggerPresenceDiff(joins, leaves)
            }
            message.event == RealtimeChannel.CHANNEL_EVENT_PRESENCE_STATE -> {
                val joins = message.payload.decodeIfNotEmptyOrDefault(mapOf<String, Presence>())
                callbackManager.triggerPresenceDiff(joins, mapOf())
            }
        }
    }

    override suspend fun leave() {
        _status.value = RealtimeChannel.Status.LEAVING
        Napier.d { "Leaving channel $topic" }
        realtimeImpl.ws?.sendSerialized(RealtimeMessage(topic, RealtimeChannel.CHANNEL_EVENT_LEAVE, buildJsonObject {}, null))
    }

    override suspend fun updateAuth(jwt: String) {
        Napier.d { "Updating auth token for channel $topic" }
        realtimeImpl.ws?.sendSerialized(RealtimeMessage(topic, RealtimeChannel.CHANNEL_EVENT_ACCESS_TOKEN, buildJsonObject {
            put("access_token", "test")
        }, (++realtimeImpl.ref).toString()))
    }

    override suspend fun broadcast(event: String, message: JsonObject) {
        if(status.value != RealtimeChannel.Status.JOINED) throw IllegalStateException("Cannot broadcast to a channel you didn't join. Did you forget to call join()?")
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
            putJsonObject(state)
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
    if(status.value == RealtimeChannel.Status.JOINED) throw IllegalStateException("You cannot call postgresChangeFlow after joining the channel")
    val event = when(T::class) {
        PostgresAction.Insert::class -> "INSERT"
        PostgresAction.Update::class -> "UPDATE"
        PostgresAction.Delete::class -> "DELETE"
        PostgresAction.Select::class -> "SELECT"
        PostgresAction::class -> "*"
        else -> throw IllegalStateException("Unknown event type ${T::class}")
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
inline fun <reified T> RealtimeChannel.broadcastFlow(event: String, json: Json = Json): Flow<T> = callbackFlow {
    val id = callbackManager.addBroadcastCallback(event) {
        val decodedValue = try {
            json.decodeFromJsonElement<T>(it)
        } catch(e: Exception) {
            Napier.e(e) { "Couldn't decode $this as ${T::class.simpleName}. The corresponding handler wasn't called" }
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
suspend inline fun <reified T> RealtimeChannel.broadcast(event: String, message: T, json: Json = Json) = broadcast(event, json.encodeToJsonElement(message).jsonObject)

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
suspend inline fun <reified T> RealtimeChannel.track(state: T, json: Json = Json) = track(json.encodeToJsonElement(state).jsonObject)