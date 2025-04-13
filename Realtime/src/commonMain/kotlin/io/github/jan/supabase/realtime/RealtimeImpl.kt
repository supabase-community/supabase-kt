package io.github.jan.supabase.realtime

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.buildUrl
import io.github.jan.supabase.collections.AtomicMutableMap
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.exceptions.UnknownRestException
import io.github.jan.supabase.logging.d
import io.github.jan.supabase.logging.e
import io.github.jan.supabase.logging.i
import io.github.jan.supabase.logging.w
import io.github.jan.supabase.realtime.websocket.KtorRealtimeWebsocketFactory
import io.github.jan.supabase.realtime.websocket.RealtimeWebsocket
import io.ktor.client.statement.HttpResponse
import io.ktor.http.URLProtocol
import io.ktor.http.path
import io.ktor.util.decodeBase64String
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import kotlin.coroutines.coroutineContext
import kotlin.io.encoding.ExperimentalEncodingApi

@PublishedApi internal class RealtimeImpl(override val supabaseClient: SupabaseClient, override val config: Realtime.Config) : Realtime {

    private val websocketFactory = config.websocketFactory ?: KtorRealtimeWebsocketFactory(supabaseClient.httpClient.httpClient)
    private var ws: RealtimeWebsocket? = null
    @Suppress("MagicNumber")
    private val _status = MutableStateFlow(Realtime.Status.DISCONNECTED)
    override val status: StateFlow<Realtime.Status> = _status.asStateFlow()
    private val _subscriptions = AtomicMutableMap<String, RealtimeChannel>()
    override val subscriptions: Map<String, RealtimeChannel> = _subscriptions
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val mutex = Mutex()
    internal var accessToken by atomic<String?>(null)
    var heartbeatJob: Job? = null
    var messageJob: Job? = null
    var ref by atomic(0)
    var heartbeatRef by atomic(0)
    override val apiVersion: Int
        get() = Realtime.API_VERSION

    override val pluginKey: String
        get() = Realtime.key

    override var serializer = config.serializer ?: supabaseClient.defaultSerializer
    private val websocketUrl = realtimeWebsocketUrl()
    private var incrementId by atomic(0)

    override suspend fun connect() = connect(false)

    suspend fun connect(reconnect: Boolean): Unit = mutex.withLock {
        if (reconnect) {
            delay(config.reconnectDelay)
            Realtime.logger.d { "Reconnecting..." }
        }
        if (status.value == Realtime.Status.CONNECTED) return
        _status.value = Realtime.Status.CONNECTING
        try {
            ws = websocketFactory.create(websocketUrl)
            _status.value = Realtime.Status.CONNECTED
            Realtime.logger.i { "Connected to realtime websocket!" }
            listenForMessages()
            startHeartbeating()
            if(reconnect) {
                rejoinChannels()
            }
        } catch(e: Exception) {
            coroutineContext.ensureActive()
            Realtime.logger.e(e) { """
                Error while trying to connect to realtime websocket. Trying again in ${config.reconnectDelay}
                URL: $websocketUrl
                """.trimIndent() }
            reconnect()
        }
    }

    override fun init() {
        scope.launch {
            supabaseClient.pluginManager.getPluginOrNull(Auth)?.sessionStatus?.collect {
                if(status.value == Realtime.Status.CONNECTED) {
                    when(it) {
                        is SessionStatus.Authenticated -> setAuth(it.session.accessToken)
                        is SessionStatus.NotAuthenticated -> {
                            if(config.disconnectOnSessionLoss) {
                                Realtime.logger.w { "No auth session found, disconnecting from realtime websocket"}
                                disconnect()
                            }
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    private fun rejoinChannels() {
        scope.launch {
            for (channel in _subscriptions.values) {
                channel.subscribe()
            }
        }
    }

    private fun listenForMessages() {
        messageJob = scope.launch {
            try {
                ws?.let {
                    while(ws?.hasIncomingMessages == true) {
                        onMessage(ws?.receive() ?: return@launch)
                    }
                }
            } catch(e: Exception) {
                coroutineContext.ensureActive()
                Realtime.logger.e(e) { "Error while listening for messages. Trying again in ${config.reconnectDelay}" }
                reconnect()
            }
        }
    }

    private fun startHeartbeating() {
        heartbeatJob = scope.launch {
            while (isActive) {
                delay(config.heartbeatInterval)
                if(!isActive) break
                launch {
                    sendHeartbeat()
                }
            }
        }
    }

    override fun disconnect() {
        Realtime.logger.d { "Closing websocket connection" }
        messageJob?.cancel()
        ws?.disconnect()
        ws = null
        heartbeatJob?.cancel()
        _status.value = Realtime.Status.DISCONNECTED
    }

    override fun channel(channelId: String, builder: RealtimeChannelBuilder): RealtimeChannel {
        val topic = RealtimeTopic.withChannelId(channelId)
        if(subscriptions.containsKey(topic)) return subscriptions[topic]!!
        val channel = builder.build(this)
        _subscriptions[topic] = channel
        return channel
    }

    private suspend fun onMessage(message: RealtimeMessage) {
        Realtime.logger.d { "Received message $message" }
        val channel = subscriptions[message.topic] as? RealtimeChannelImpl
        if(message.ref?.toIntOrNull() == heartbeatRef) {
            Realtime.logger.i { "Heartbeat received" }
            heartbeatRef = 0
        } else {
            Realtime.logger.d { "Received event ${message.event} for channel ${channel?.topic}" }
            channel?.onMessage(message)
        }
    }

    @OptIn(ExperimentalEncodingApi::class)
    override suspend fun setAuth(token: String?) {
        val newToken = token ?: config.accessToken(supabaseClient)

        if(newToken != null) {
            val parsed = Json.decodeFromString<JsonObject>(newToken.split(".")[1].decodeBase64String())
            val exp = parsed["exp"]?.jsonPrimitive?.longOrNull ?: error("No exp found in token")
            val now = Clock.System.now().epochSeconds
            val diff = exp - now
            if(diff < 0) {
                Realtime.logger.w { "Token is expired. Not sending it to realtime." }
                return
            }
        }
        this.accessToken = newToken
        scope.launch {
            subscriptions.values.filter { it.status.value == RealtimeChannel.Status.SUBSCRIBED }.forEach { it.updateAuth(accessToken) }
        }
    }

    private suspend fun sendHeartbeat() {
        if (heartbeatRef != 0) {
            heartbeatRef = 0
            ref = 0
            Realtime.logger.e { "Heartbeat timeout. Trying to reconnect in ${config.reconnectDelay}" }
            reconnect()
            return
        }
        Realtime.logger.d { "Sending heartbeat" }
        heartbeatRef = ++ref
        send(RealtimeMessage("phoenix", "heartbeat", buildJsonObject { }, heartbeatRef.toString()))
    }

    override suspend fun removeChannel(channel: RealtimeChannel) {
        if(channel.status.value == RealtimeChannel.Status.SUBSCRIBED) {
            channel.unsubscribe()
        }
        _subscriptions.remove(channel.topic)
        if(subscriptions.isEmpty() && config.disconnectOnNoSubscriptions) {
            Realtime.logger.d { "No more subscriptions, disconnecting from realtime websocket" }
            disconnect()
        }
    }

    override suspend fun removeAllChannels() {
        _subscriptions.forEach { (_, it) ->
            if(it.status.value == RealtimeChannel.Status.SUBSCRIBED) {
                it.unsubscribe()
            }
        }
        _subscriptions.clear()
        if(config.disconnectOnNoSubscriptions) {
            Realtime.logger.d { "No more subscriptions, disconnecting from realtime websocket" }
            disconnect()
        }
    }

    override suspend fun close() {
        disconnect()
    }

    override suspend fun block() {
        ws?.blockUntilDisconnect() ?: error("No connection available")
    }

    override suspend fun parseErrorResponse(response: HttpResponse): RestException {
        return UnknownRestException("Unknown error in realtime plugin", response)
    }

    private fun realtimeBaseUrl(): String {
        val secure = config.secure ?: supabaseClient.useHTTPS
        val prefix = if (secure) "wss://" else "ws://"
        val baseUrl = config.customUrl
            ?: (prefix + supabaseClient.supabaseUrl)
        return buildUrl(baseUrl) {
            path("/realtime/v${Realtime.API_VERSION}")
        }
    }

    private fun realtimeWebsocketUrl(): String {
        return buildUrl(realtimeBaseUrl()) {
            parameters["apikey"] = supabaseClient.supabaseKey
            parameters["vsn"] = "1.0.0"
            pathSegments += listOf("websocket")
        }
    }

    fun broadcastUrl(): String {
        val secure = config.secure ?: supabaseClient.useHTTPS
        return buildUrl(realtimeBaseUrl()) {
            protocol = if(secure) URLProtocol.HTTPS else URLProtocol.HTTP
            pathSegments += listOf("api", "broadcast")
        }
    }

    override suspend fun send(message: RealtimeMessage) {
        try {
            ws?.send(message)
        } catch(e: Exception) {
            coroutineContext.ensureActive()
            Realtime.logger.e(e) { "Error while sending message $message. Reconnecting in ${config.reconnectDelay}" }
            reconnect()
        }
    }

    fun nextIncrementId(): Int {
        return incrementId++
    }

    private fun reconnect() {
        scope.launch {
            disconnect()
            connect(true)
        }
    }

}
