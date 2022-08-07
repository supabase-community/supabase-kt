package io.github.jan.supacompose.realtime

import io.github.aakira.napier.Napier
import io.github.jan.supacompose.annotiations.SupaComposeInternal
import io.github.jan.supacompose.realtime.events.ChannelAction
import io.github.jan.supacompose.realtime.events.EventListener
import io.github.jan.supacompose.supabaseJson
import io.ktor.client.plugins.websocket.sendSerialized
import io.ktor.http.ContentType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

sealed interface RealtimeChannel {

    val status: StateFlow<Status>
    val topic: String
    val schema: String
    val table: String?
    val column: String?
    val value: String?
    suspend fun join()
    suspend fun updateAuth(jwt: String)
    suspend fun leave()
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
        const val CHANNEL_EVENT_ACCESS_TOKEN = "access_token"
    }

}

internal class RealtimeChannelImpl(
    private val realtimeImpl: RealtimeImpl,
    override val topic: String,
    override val schema: String,
    override val table: String?,
    override val column: String?,
    override val value: String?,
    private var jwt: String,
    private val listeners: MutableList<EventListener>
) : RealtimeChannel {

    private val _status = MutableStateFlow(RealtimeChannel.Status.CLOSED)
    override val status = _status.asStateFlow()

    @OptIn(SupaComposeInternal::class)
    override suspend fun join() {
        realtimeImpl.addChannel(this)
        _status.value = RealtimeChannel.Status.JOINING
        Napier.d { "Joining channel $topic" }
        realtimeImpl.ws.sendSerialized(RealtimeMessage(topic, RealtimeChannel.CHANNEL_EVENT_JOIN, buildJsonObject {
            put("user_token", jwt)
        }, null))
    }

    @OptIn(SupaComposeInternal::class)
    fun onMessage(message: RealtimeMessage) {
        when {
            message.payload["status"]?.jsonPrimitive?.content == "ok" -> {
                Napier.d { "Joined channel ${message.topic}" }
                _status.value = RealtimeChannel.Status.JOINED
            }
            message.event in listOf("UPDATE", "DELETE", "INSERT", "SELECT") -> {
                val action = when(message.event) {
                    "UPDATE" -> supabaseJson.decodeFromJsonElement<ChannelAction.Update>(message.payload)
                    "DELETE" -> supabaseJson.decodeFromJsonElement<ChannelAction.Delete>(message.payload)
                    "INSERT" -> supabaseJson.decodeFromJsonElement<ChannelAction.Insert>(message.payload)
                    "SELECT" -> supabaseJson.decodeFromJsonElement<ChannelAction.Select>(message.payload)
                    else -> throw IllegalStateException("Unknown event type ${message.event}")
                }
                listeners.forEach { it.onEvent(action) }
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

}