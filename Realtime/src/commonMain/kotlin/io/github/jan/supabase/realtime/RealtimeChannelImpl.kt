package io.github.jan.supabase.realtime

import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.collections.AtomicMutableList
import io.github.jan.supabase.gotrue.resolveAccessToken
import io.github.jan.supabase.logging.d
import io.github.jan.supabase.logging.e
import io.github.jan.supabase.putJsonObject
import io.github.jan.supabase.realtime.data.BroadcastApiBody
import io.github.jan.supabase.realtime.data.BroadcastApiMessage
import io.github.jan.supabase.realtime.event.RealtimeEvent
import io.ktor.client.statement.bodyAsText
import io.ktor.http.headers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

internal class RealtimeChannelImpl(
    private val realtimeImpl: RealtimeImpl,
    override val topic: String,
    private val broadcastJoinConfig: BroadcastJoinConfig,
    private val presenceJoinConfig: PresenceJoinConfig,
    private val isPrivate: Boolean,
) : RealtimeChannel {

    private val clientChanges = AtomicMutableList<PostgresJoinConfig>()
    @SupabaseInternal
    override val callbackManager = CallbackManagerImpl(realtimeImpl.serializer)
    private val _status = MutableStateFlow(RealtimeChannel.Status.UNSUBSCRIBED)
    override val status = _status.asStateFlow()
    override val realtime: Realtime = realtimeImpl

    override val supabaseClient = realtimeImpl.supabaseClient

    private val broadcastUrl = realtimeImpl.broadcastUrl()
    private val subTopic = topic.replaceFirst(Regex("^realtime:", RegexOption.IGNORE_CASE), "")
    private val httpClient = realtimeImpl.supabaseClient.httpClient

    @OptIn(SupabaseInternal::class)
    override suspend fun subscribe(blockUntilSubscribed: Boolean) {
        if(realtimeImpl.status.value != Realtime.Status.CONNECTED) {
            if(!realtimeImpl.config.connectOnSubscribe) error("You can't subscribe to a channel while the realtime client is not connected. Did you forget to call `realtime.connect()`?")
            realtimeImpl.connect()
        }
        realtimeImpl.run {
            addChannel(this@RealtimeChannelImpl)
        }
        _status.value = RealtimeChannel.Status.SUBSCRIBING
        Realtime.logger.d { "Subscribing to channel $topic" }
        val currentJwt = supabaseClient.resolveAccessToken(realtimeImpl, keyAsFallback = false)
        val postgrestChanges = clientChanges.toList()
        val joinConfig = RealtimeJoinPayload(RealtimeJoinConfig(broadcastJoinConfig, presenceJoinConfig, postgrestChanges, isPrivate))
        val joinConfigObject = buildJsonObject {
            putJsonObject(Json.encodeToJsonElement(joinConfig).jsonObject)
            currentJwt?.let {
                put("access_token", currentJwt)
            }
        }
        Realtime.logger.d { "Subscribing to channel with body $joinConfigObject" }
        realtimeImpl.send(
            RealtimeMessage(topic, RealtimeChannel.CHANNEL_EVENT_JOIN, joinConfigObject, null)
        )
        if(blockUntilSubscribed) {
            status.first { it == RealtimeChannel.Status.SUBSCRIBED }
        }
    }

    @OptIn(SupabaseInternal::class)
    fun onMessage(message: RealtimeMessage) {
        val event = RealtimeEvent.resolveEvent(message)
        if(event == null) {
            Realtime.logger.e { "Received message without event: $message" }
            return
        }
        event.handle(this, message)
    }

    override suspend fun unsubscribe() {
        _status.value = RealtimeChannel.Status.UNSUBSCRIBING
        Realtime.logger.d { "Unsubscribing from channel $topic" }
        realtimeImpl.send(RealtimeMessage(topic, RealtimeChannel.CHANNEL_EVENT_LEAVE, buildJsonObject {}, null))
    }

    override suspend fun updateAuth(jwt: String) {
        Realtime.logger.d { "Updating auth token for channel $topic" }
        realtimeImpl.send(RealtimeMessage(topic, RealtimeChannel.CHANNEL_EVENT_ACCESS_TOKEN, buildJsonObject {
            put("access_token", jwt)
        }, (++realtimeImpl.ref).toString()))
    }

    override suspend fun broadcast(event: String, message: JsonObject) {
        if(status.value != RealtimeChannel.Status.SUBSCRIBED) {
            val response = httpClient.postJson(
                url = broadcastUrl,
                body = BroadcastApiBody(listOf(BroadcastApiMessage(subTopic, event, message, isPrivate)))
            ) {
                headers {
                    append("apikey", realtimeImpl.supabaseClient.supabaseKey)
                }
            }
            @Suppress("MagicNumber")
            if(response.status.value !in 200..299) {
                error("Failed to broadcast message (${response.status}): ${response.bodyAsText()}")
            }
        } else {
            realtimeImpl.send(
                RealtimeMessage(topic, "broadcast", buildJsonObject {
                    put("type", "broadcast")
                    put("event", event)
                    put("payload", message)
                }, (++realtimeImpl.ref).toString())
            )
        }
    }

    @SupabaseInternal
    override fun RealtimeChannel.addPostgresChange(data: PostgresJoinConfig) {
        clientChanges.add(data)
    }

    @SupabaseInternal
    override fun RealtimeChannel.removePostgresChange(data: PostgresJoinConfig) {
        clientChanges.remove(data)
    }

    override suspend fun track(state: JsonObject) {
        if(status.value != RealtimeChannel.Status.SUBSCRIBED) {
            error("You can only track your presence after subscribing to the channel. Did you forget to call `channel.subscribe()`?")
        }
        val payload = buildJsonObject {
            put("type", "presence")
            put("event", "track")
            putJsonObject("payload") {
                putJsonObject(state)
            }
        }
        realtimeImpl.send(
            RealtimeMessage(topic, RealtimeChannel.CHANNEL_EVENT_PRESENCE, payload, (++realtimeImpl.ref).toString())
        )
    }

    override suspend fun untrack() {
        realtimeImpl.send(
            RealtimeMessage(topic, RealtimeChannel.CHANNEL_EVENT_PRESENCE, buildJsonObject {
                put("type", "presence")
                put("event", "untrack")
            }, (++realtimeImpl.ref).toString())
        )
    }

    override fun updateStatus(status: RealtimeChannel.Status) {
        _status.value = status
    }

}

