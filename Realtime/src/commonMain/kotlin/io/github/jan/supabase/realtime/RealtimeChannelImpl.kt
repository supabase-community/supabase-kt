package io.github.jan.supabase.realtime

import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.collections.AtomicMutableList
import io.github.jan.supabase.logging.d
import io.github.jan.supabase.logging.e
import io.github.jan.supabase.putJsonObject
import io.github.jan.supabase.realtime.data.BroadcastApiBody
import io.github.jan.supabase.realtime.data.BroadcastApiMessage
import io.github.jan.supabase.realtime.event.RealtimeEvent
import io.ktor.client.statement.bodyAsText
import io.ktor.http.headers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import kotlin.reflect.KClass
import kotlin.reflect.KType

internal class RealtimeChannelImpl(
    override val realtime: Realtime,
    override val topic: String,
    private val broadcastJoinConfig: BroadcastJoinConfig,
    private val presenceJoinConfig: PresenceJoinConfig,
    private val isPrivate: Boolean,
) : RealtimeChannel {

    private val realtimeImpl: RealtimeImpl = realtime as RealtimeImpl
    private val clientChanges = AtomicMutableList<PostgresJoinConfig>()
    @SupabaseInternal
    override val callbackManager = CallbackManagerImpl(realtimeImpl.serializer)
    private val _status = MutableStateFlow(RealtimeChannel.Status.UNSUBSCRIBED)
    override val status = _status.asStateFlow()
    override val supabaseClient = realtimeImpl.supabaseClient

    private val broadcastUrl = realtimeImpl.broadcastUrl()
    private val subTopic = topic.replaceFirst(Regex("^${RealtimeTopic.PREFIX}:", RegexOption.IGNORE_CASE), "")
    private val httpClient = realtimeImpl.supabaseClient.httpClient

    private suspend fun accessToken() = realtimeImpl.config.accessToken(supabaseClient) ?: realtimeImpl.accessToken

    @OptIn(SupabaseInternal::class)
    override suspend fun subscribe(blockUntilSubscribed: Boolean) {
        if(realtimeImpl.status.value != Realtime.Status.CONNECTED) {
            if(!realtimeImpl.config.connectOnSubscribe) error("You can't subscribe to a channel while the realtime client is not connected. Did you forget to call `realtime.connect()`?")
            realtimeImpl.connect()
        }
        _status.value = RealtimeChannel.Status.SUBSCRIBING
        Realtime.logger.d { "Subscribing to channel $topic" }
        val currentJwt = accessToken()
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
    suspend fun onMessage(message: RealtimeMessage) {
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

    override suspend fun updateAuth(jwt: String?) {
        Realtime.logger.d { "Updating auth token for channel $topic" }
        realtimeImpl.send(RealtimeMessage(topic, RealtimeChannel.CHANNEL_EVENT_ACCESS_TOKEN, buildJsonObject {
            put("access_token", jwt)
        }, (++realtimeImpl.ref).toString()))
    }

    override suspend fun broadcast(event: String, message: JsonObject) {
        if(status.value != RealtimeChannel.Status.SUBSCRIBED) {
            val token = accessToken()
            val response = httpClient.postJson(
                url = broadcastUrl,
                body = BroadcastApiBody(listOf(BroadcastApiMessage(subTopic, event, message, isPrivate)))
            ) {
                headers {
                    append("apikey", realtimeImpl.supabaseClient.supabaseKey)
                    token?.let {
                        set("Authorization", "Bearer $it")
                    }
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

    @Suppress("UNCHECKED_CAST")
    override fun <T : PostgresAction> RealtimeChannel.postgresChangeFlowInternal(
        action: KClass<T>,
        schema: String,
        filter: PostgresChangeFilter.() -> Unit
    ): Flow<T> {
        if(status.value == RealtimeChannel.Status.SUBSCRIBED) error("You cannot call postgresChangeFlow after joining the channel")
        val event = when(action) {
            PostgresAction.Insert::class -> "INSERT"
            PostgresAction.Update::class -> "UPDATE"
            PostgresAction.Delete::class -> "DELETE"
            PostgresAction.Select::class -> "SELECT"
            PostgresAction::class -> "*"
            else -> error("Unknown event type $action")
        }
        val postgrestBuilder = PostgresChangeFilter(event, schema).apply(filter)
        val config = postgrestBuilder.buildConfig()
        addPostgresChange(config)
        return callbackFlow {
            val callback: (PostgresAction) -> Unit = {
                if (action.isInstance(it)) {
                    trySend(it as T)
                }
            }

            val id = callbackManager.addPostgresCallback(config, callback)
            awaitClose {
                callbackManager.removeCallbackById(id)
                removePostgresChange(config)
            }
        }
    }

    override fun <T : Any> RealtimeChannel.broadcastFlowInternal(type: KType, event: String): Flow<T> = callbackFlow {
        val id = callbackManager.addBroadcastCallback(event) {
            val decodedValue = try {
                supabaseClient.realtime.serializer.decode<T>(type, it.toString())
            } catch(e: Exception) {
                Realtime.logger.e(e) { "Couldn't decode $it as $type. The corresponding handler wasn't called" }
                null
            }
            decodedValue?.let { value -> trySend(value) }
        }
        awaitClose { callbackManager.removeCallbackById(id) }
    }

    override fun presenceChangeFlow(): Flow<PresenceAction> = callbackFlow {
        val callback: (PresenceAction) -> Unit = { action ->
            trySend(action)
        }
        val id = callbackManager.addPresenceCallback(callback)
        awaitClose { callbackManager.removeCallbackById(id) }
    }

    override fun updateStatus(status: RealtimeChannel.Status) {
        _status.value = status
    }

}

