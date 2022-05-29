package io.github.jan.supacompose.realtime

import io.github.aakira.napier.Napier
import io.github.jan.supacompose.supabaseJson
import io.ktor.client.plugins.websocket.sendSerialized
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put


sealed interface RealtimeChannel {

    val status: StateFlow<Status>
    val topic: String
    val schema: String?
    val table: String?
    val column: String?
    val value: String?

    suspend fun join()

    fun on(action: Action, callback: (RealtimeChannelMessage) -> Unit): RealtimeChannel

    enum class Status {
        CLOSED,
        JOINING,
        JOINED,
        LEAVING,
    }

    enum class Action {
        UPDATE,
        DELETE,
        SELECT,
        INSERT,
        ALL
    }

    companion object {
        const val CHANNEL_EVENT_JOIN = "phx_join"
    }

}

internal class RealtimeChannelImpl(
    private val realtimeImpl: RealtimeImpl,
    override val topic: String,
    override val schema: String?,
    override val table: String?,
    override val column: String?,
    override val value: String?,
    private val jwt: String
) : RealtimeChannel {

    private val _status = MutableStateFlow(RealtimeChannel.Status.CLOSED)
    override val status = _status.asStateFlow()
    private val callbacks = mutableListOf<Pair<RealtimeChannel.Action, (RealtimeChannelMessage) -> Unit>>()

    override suspend fun join() {
        _status.value = RealtimeChannel.Status.JOINING
        Napier.d { "Joining channel $topic" }
        realtimeImpl.ws.sendSerialized(RealtimeMessage(topic, RealtimeChannel.CHANNEL_EVENT_JOIN, buildJsonObject {
            put("user_token", jwt)
        }, "null"))
    }

    override fun on(action: RealtimeChannel.Action, callback: (RealtimeChannelMessage) -> Unit): RealtimeChannel {
        callbacks.add(action to callback)
        return this
    }

    fun onMessage(message: RealtimeMessage) {
        when {
            message.payload["status"]?.jsonPrimitive?.content == "ok" -> {
                Napier.d { "Joined channel ${message.topic}" }
                _status.value = RealtimeChannel.Status.JOINED
            }
            message.event in listOf("UPDATE", "DELETE", "INSERT", "SELECT") -> {
                callbacks.filter { it.first.name == message.event || it.first == RealtimeChannel.Action.ALL }.forEach {
                    it.second(supabaseJson.decodeFromJsonElement(message.payload))
                }
            }
        }
    }

}