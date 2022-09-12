package io.github.jan.supacompose.realtime

import io.github.aakira.napier.Napier
import io.github.jan.supacompose.SupabaseClient
import io.github.jan.supacompose.annotiations.SupaComposeInternal
import io.github.jan.supacompose.putJsonObject
import io.github.jan.supacompose.supabaseJson
import io.ktor.client.plugins.websocket.sendSerialized
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.datetime.Clock
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
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
import kotlin.experimental.ExperimentalTypeInference

sealed interface RealtimeChannel {

    val status: StateFlow<Status>
    val topic: String
    val supabaseClient: SupabaseClient

    @SupaComposeInternal
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

    suspend fun track(data: JsonObject)

    suspend fun untrack()

    @SupaComposeInternal
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
    }

}

internal class RealtimeChannelImpl(
    private val realtimeImpl: RealtimeImpl,
    override val topic: String,
    private val broadcastJoinConfig: BroadcastJoinConfig,
    private val presenceJoinConfig: PresenceJoinConfig,
    private var jwt: String
) : RealtimeChannel {

    private val clientChanges = mutableListOf<PostgresJoinConfig>()
    @SupaComposeInternal
    override val callbackManager = CallbackManagerImpl()
    private val _status = MutableStateFlow(RealtimeChannel.Status.CLOSED)
    override val status = _status.asStateFlow()

    override val supabaseClient = realtimeImpl.supabaseClient

    @OptIn(SupaComposeInternal::class)
    override suspend fun join() {
        realtimeImpl.addChannel(this)
        _status.value = RealtimeChannel.Status.JOINING
        Napier.d { "Joining channel $topic" }
        val postgrestChanges = clientChanges.toList()
        println(postgrestChanges.size)
        val joinConfig = RealtimeJoinPayload(RealtimeJoinConfig(broadcastJoinConfig, presenceJoinConfig, postgrestChanges))
        val joinConfigObject = buildJsonObject {
            putJsonObject(Json.encodeToJsonElement(joinConfig).jsonObject)
            if(jwt.isNotBlank()) put("access_token", jwt)
        }
        println(Json.encodeToString(RealtimeMessage(topic, RealtimeChannel.CHANNEL_EVENT_JOIN, joinConfigObject, null)))
        realtimeImpl.ws.sendSerialized(RealtimeMessage(topic, RealtimeChannel.CHANNEL_EVENT_JOIN, joinConfigObject, null))
    }

    @OptIn(SupaComposeInternal::class)
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
                println(Clock.System.now().toEpochMilliseconds())
                val event = message.payload["event"]?.jsonPrimitive?.content ?: ""
                callbackManager.triggerBroadcast(event, message.payload["payload"]?.jsonObject ?: JsonObject(mutableMapOf()))
            }
            message.event == RealtimeChannel.CHANNEL_EVENT_CLOSE -> {
                realtimeImpl.removeChannel(topic)
                Napier.d { "Left channel ${message.topic}" }
            }
            message.event == RealtimeChannel.CHANNEL_EVENT_ERROR -> {
                Napier.e { "Received an error in channel ${message.topic}. That could be as a result of an invalid access token" }
            }
        }
    }

    override suspend fun leave() {
        _status.value = RealtimeChannel.Status.LEAVING
        Napier.d { "Leaving channel $topic" }
        realtimeImpl.ws.sendSerialized(RealtimeMessage(topic, RealtimeChannel.CHANNEL_EVENT_LEAVE, buildJsonObject {}, null))
    }

    override suspend fun updateAuth(jwt: String) {
        this.jwt = jwt
        Napier.d { "Updating auth token for channel $topic" }
        realtimeImpl.ws.sendSerialized(RealtimeMessage(topic, RealtimeChannel.CHANNEL_EVENT_ACCESS_TOKEN, buildJsonObject {
            put("access_token", "test")
        }, (++realtimeImpl.ref).toString()))
    }

    override suspend fun broadcast(event: String, message: JsonObject) {
        realtimeImpl.ws.sendSerialized(RealtimeMessage(topic, "broadcast", buildJsonObject {
            put("type", "broadcast")
            put("event", event)
            put("payload", message)
        }, (++realtimeImpl.ref).toString()))
    }

    @SupaComposeInternal
    override fun RealtimeChannel.addPostgresChange(data: PostgresJoinConfig) {
        clientChanges.add(data)
    }

    override suspend fun track(data: JsonObject) {
        val payload = buildJsonObject {
            put("type", "presence")
            put("event", "track")
            putJsonObject(data)
        }
        realtimeImpl.ws.sendSerialized(RealtimeMessage(topic, RealtimeChannel.CHANNEL_EVENT_PRESENCE, payload, (++realtimeImpl.ref).toString()))
    }

    override suspend fun untrack() {
        realtimeImpl.ws.sendSerialized(RealtimeMessage(topic, RealtimeChannel.CHANNEL_EVENT_PRESENCE, buildJsonObject {
            put("type", "presence")
            put("event", "untrack")
        }, (++realtimeImpl.ref).toString()))
    }

}

@OptIn(SupaComposeInternal::class)
inline fun <reified T : PostgresAction> RealtimeChannel.postgrestChangeFlow(builder: PostgresChangeBuilder.() -> Unit): Flow<T> {
    val event = when(T::class) {
        PostgresAction.Insert::class -> "INSERT"
        PostgresAction.Update::class -> "UPDATE"
        PostgresAction.Delete::class -> "DELETE"
        PostgresAction.Select::class -> "SELECT"
        PostgresAction::class -> "*"
        else -> throw IllegalStateException("Unknown event type ${T::class}")
    }
    val postgrestBuilder = PostgresChangeBuilder(event).apply(builder)
    val config = postgrestBuilder.buildConfig()
    addPostgresChange(config)
    return callbackFlow {
        val callback: (PostgresAction) -> Unit = {
            if (it is T) {
                trySend(it)
            }
        }

        val id = callbackManager.addPostgresCallback(config, callback)
        awaitClose { callbackManager.removePostgresCallbackById(id) }
    }
}

@OptIn(SupaComposeInternal::class)
inline fun <reified T> RealtimeChannel.onBroadcast(event: String, json: Json = Json, crossinline handler: T.() -> Unit) {
    callbackManager.addBroadcastCallback(event) {
        val decodedValue = try {
            json.decodeFromJsonElement<T>(it)
        } catch(e: Exception) {
            Napier.e(e) { "Couldn't decode $this as ${T::class.simpleName}. The corresponding handler wasn't called" }
            null
        }
        decodedValue?.let { handler(it) }
    }
}

@OptIn(SupaComposeInternal::class)
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
    awaitClose { callbackManager.removeBroadcastCallbackById(id) }
}

/**
 * Sends a message to everyone who joined the channel
 * @param event the broadcast event. Example: mouse_cursor
 * @param message the message to send as [T]
 */
suspend inline fun <reified T> RealtimeChannel.broadcast(event: String, message: T) = broadcast(event, Json.encodeToJsonElement(message).jsonObject)

suspend inline fun <reified T> RealtimeChannel.track(data: T) = track(Json.encodeToJsonElement(data).jsonObject)