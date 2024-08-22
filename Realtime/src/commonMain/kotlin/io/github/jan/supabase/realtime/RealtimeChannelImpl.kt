package io.github.jan.supabase.realtime

import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.collections.AtomicMutableList
import io.github.jan.supabase.decodeIfNotEmptyOrDefault
import io.github.jan.supabase.gotrue.resolveAccessToken
import io.github.jan.supabase.logging.d
import io.github.jan.supabase.logging.e
import io.github.jan.supabase.logging.w
import io.github.jan.supabase.putJsonObject
import io.github.jan.supabase.realtime.data.BroadcastApiBody
import io.github.jan.supabase.realtime.data.BroadcastApiMessage
import io.github.jan.supabase.realtime.data.PostgresActionData
import io.github.jan.supabase.supabaseJson
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
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import kotlin.reflect.KClass
import kotlin.reflect.KType

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
    @Suppress("CyclomaticComplexMethod") //TODO: Refactor this method
    fun onMessage(message: RealtimeMessage) {
        if(message.eventType == null) {
            Realtime.logger.e { "Received message without event type: $message" }
            return
        }
        when(message.eventType) {
            RealtimeMessage.EventType.TOKEN_EXPIRED -> {
                Realtime.logger.w { "Received token expired event. This should not happen, please report this warning." }
            }
            RealtimeMessage.EventType.SYSTEM -> {
                Realtime.logger.d { "Subscribed to channel ${message.topic}" }
                _status.value = RealtimeChannel.Status.SUBSCRIBED
            }
            RealtimeMessage.EventType.SYSTEM_REPLY -> {
                Realtime.logger.d { "Received system reply: ${message.payload}." }
                if(status.value == RealtimeChannel.Status.UNSUBSCRIBING) {
                    _status.value = RealtimeChannel.Status.UNSUBSCRIBED
                    Realtime.logger.d { "Unsubscribed from channel ${message.topic}" }
                }
            }
            RealtimeMessage.EventType.POSTGRES_SERVER_CHANGES -> { //check if the server postgres_changes match with the client's and add the given id to the postgres change objects (to identify them later in the events)
                val serverPostgresChanges = message.payload["response"]?.jsonObject?.get("postgres_changes")?.jsonArray?.let { Json.decodeFromJsonElement<List<PostgresJoinConfig>>(it) } ?: listOf() //server postgres changes
                callbackManager.setServerChanges(serverPostgresChanges)
                if(status.value != RealtimeChannel.Status.SUBSCRIBED) {
                    Realtime.logger.d { "Joined channel ${message.topic}" }
                    _status.value = RealtimeChannel.Status.SUBSCRIBED
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
                Realtime.logger.d { "Unsubscribed from channel ${message.topic}" }
            }
            RealtimeMessage.EventType.ERROR -> {
                Realtime.logger.e { "Received an error in channel ${message.topic}. That could be as a result of an invalid access token" }
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

}

