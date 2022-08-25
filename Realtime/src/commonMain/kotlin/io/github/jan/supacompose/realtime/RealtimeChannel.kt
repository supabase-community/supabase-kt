package io.github.jan.supacompose.realtime

import io.github.aakira.napier.Napier
import io.github.jan.supacompose.annotiations.SupaComposeInternal
import io.github.jan.supacompose.putJsonObject
import io.github.jan.supacompose.supabaseJson
import io.ktor.client.plugins.websocket.sendSerialized
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

sealed interface RealtimeChannel {

    val status: StateFlow<Status>
    val topic: String

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
    }

}

internal class RealtimeChannelImpl(
    private val realtimeImpl: RealtimeImpl,
    override val topic: String,
    private val bindings: MutableMap<String, List<RealtimeBinding>>,
    private var jwt: String
) : RealtimeChannel {

    private val _status = MutableStateFlow(RealtimeChannel.Status.CLOSED)
    override val status = _status.asStateFlow()

    @OptIn(SupaComposeInternal::class)
    override suspend fun join() {
        realtimeImpl.addChannel(this)
        _status.value = RealtimeChannel.Status.JOINING
        Napier.d { "Joining channel $topic" }
        val postgrestChanges = bindings.getOrElse("postgres_changes") { listOf() }.map { (it as RealtimeBinding.PostgrestRealtimeBinding).filter }
        val joinConfig = RealtimeJoinPayload(RealtimeJoinConfig(BroadcastJoinConfig(false, false), PresenceJoinConfig(""), postgrestChanges))
        val joinConfigObject = buildJsonObject {
            putJsonObject(Json.encodeToJsonElement(joinConfig).jsonObject)
            if(jwt.isNotBlank()) put("access_token", jwt)
        }
        realtimeImpl.ws.sendSerialized(RealtimeMessage(topic, RealtimeChannel.CHANNEL_EVENT_JOIN, joinConfigObject, null))
    }

    @OptIn(SupaComposeInternal::class)
    fun onMessage(message: RealtimeMessage) {
        when {
            message.event == "system" && message.payload["status"]?.jsonPrimitive?.content == "ok" -> {
                Napier.d { "Joined channel ${message.topic}" }
                _status.value = RealtimeChannel.Status.JOINED
            }
            message.event == RealtimeChannel.CHANNEL_EVENT_REPLY && message.payload["response"]?.jsonObject?.containsKey("postgres_changes") ?: false -> {
                val serverPostgresChanges = message.payload["response"]?.jsonObject?.get("postgres_changes")?.jsonArray?.let { Json.decodeFromJsonElement<List<PostgresJoinConfig>>(it) } ?: listOf()
                val currentPostgresChanges = bindings.getOrElse("postgres_changes") { listOf() }.map { (it as RealtimeBinding.PostgrestRealtimeBinding) }
                val newPostgresChanges = mutableListOf<RealtimeBinding.PostgrestRealtimeBinding>()
                for ((index, binding) in currentPostgresChanges.withIndex()) {
                    val serverPostgresChange = serverPostgresChanges[index]
                    if(serverPostgresChange.event != binding.filter.event || serverPostgresChange.schema != binding.filter.schema || serverPostgresChange.table != binding.filter.table) {
                        Napier.e { "Postgres change biding mismatch between server and client" }
                        //leave channel
                        break
                    } else {
                        newPostgresChanges.add(binding.copy(filter = binding.filter.copy(id = serverPostgresChange.id)))
                    }
                }
                bindings["postgres_changes"] = newPostgresChanges
            }
            message.event == "postgres_changes" -> {
                val data = message.payload["data"]?.jsonObject ?: return
                val ids = message.payload["ids"]?.jsonArray?.map { it.jsonPrimitive.content } ?: emptyList()
                val event = data["type"]?.jsonPrimitive?.content ?: ""
                val callbacks = bindings.getOrElse("postgres_changes") { listOf() }.map { (it as RealtimeBinding.PostgrestRealtimeBinding) }.filter {
                    it.filter.id in ids
                }
                val action = when(event) {
                    "UPDATE" -> supabaseJson.decodeFromJsonElement<PostgresAction.Update>(data)
                    "DELETE" -> supabaseJson.decodeFromJsonElement<PostgresAction.Delete>(data)
                    "INSERT" -> supabaseJson.decodeFromJsonElement<PostgresAction.Insert>(data)
                    "SELECT" -> supabaseJson.decodeFromJsonElement<PostgresAction.Select>(data)
                    else -> throw IllegalStateException("Unknown event type ${message.event}")
                }
                callbacks.forEach {
                    it.callback(action)
                }
            }
            message.event == RealtimeChannel.CHANNEL_EVENT_BROADCAST -> {
                val event = message.payload["event"]?.jsonPrimitive?.content ?: ""
                val callbacks = bindings.getOrElse("broadcast") { listOf() }.filter { (it as RealtimeBinding.DefaultRealtimeBinding).filter == event }
                callbacks.forEach { it.callback(Json.encodeToString(message.payload["payload"]?.jsonObject)) }
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

}

/**
 * Sends a message to everyone who joined the channel
 * @param event the broadcast event. Example: mouse_cursor
 * @param message the message to send as [T]
 */
suspend inline fun <reified T> RealtimeChannel.broadcast(event: String, message: T) = broadcast(event, Json.encodeToJsonElement(message).jsonObject)