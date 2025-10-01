package io.github.jan.supabase.realtime

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.buildUrl
import io.github.jan.supabase.collections.AtomicMutableMap
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.exceptions.UnknownRestException
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.SessionStatus
import io.github.jan.supabase.logging.d
import io.github.jan.supabase.logging.e
import io.github.jan.supabase.logging.i
import io.github.jan.supabase.logging.w
import io.github.jan.supabase.supabaseJson
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.sendSerialized
import io.ktor.client.statement.HttpResponse
import io.ktor.http.URLProtocol
import io.ktor.http.path
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.buildJsonObject
import kotlin.time.Duration.Companion.milliseconds

@PublishedApi internal class RealtimeImpl(override val supabaseClient: SupabaseClient, override val config: Realtime.Config) : Realtime {

    private var ws: DefaultClientWebSocketSession? = null
    @Suppress("MagicNumber")
    private val msPerEvent = 1000 / config.eventsPerSecond
    private val _status = MutableStateFlow(Realtime.Status.DISCONNECTED)
    override val status: StateFlow<Realtime.Status> = _status.asStateFlow()
    private val _subscriptions = AtomicMutableMap<String, RealtimeChannel>()
    override val subscriptions: Map<String, RealtimeChannel>
        get() = _subscriptions.toMap()
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    internal val coroutineScope: CoroutineScope
        get() = scope
    private val mutex = Mutex()
    var heartbeatJob: Job? = null
    var messageJob: Job? = null
    var ref by atomic(0)
    var heartbeatRef by atomic(0)
    var inThrottle by atomic(false)
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
        val realtimeUrl = websocketUrl
        try {
            ws = supabaseClient.httpClient.webSocketSession(realtimeUrl)
            _status.value = Realtime.Status.CONNECTED
            Realtime.logger.i { "Connected to realtime websocket!" }
            listenForMessages()
            startHeartbeating()
            if(reconnect) {
                rejoinChannels()
            }
        } catch(e: Exception) {
            Realtime.logger.e(e) { """
                Error while trying to connect to realtime websocket. Trying again in ${config.reconnectDelay}
                URL: $realtimeUrl
                """.trimIndent() }
            scope.launch {
                disconnect()
                connect(true)
            }
        }
    }

    override fun init() {
        scope.launch {
            supabaseClient.pluginManager.getPluginOrNull(Auth)?.sessionStatus?.collect {
                if(status.value == Realtime.Status.CONNECTED) {
                    when(it) {
                        is SessionStatus.Authenticated -> updateJwt(it.session.accessToken)
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
                    for (frame in it.incoming) {
                        val message = frame as? Frame.Text ?: continue
                        onMessage(message.readText())
                    }
                }
            } catch(e: Exception) {
                if(!isActive) return@launch
                Realtime.logger.e(e) { "Error while listening for messages. Trying again in ${config.reconnectDelay}" }
                scope.launch {
                    disconnect()
                    connect(true)
                }
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
        ws?.cancel()
        ws = null
        heartbeatJob?.cancel()
        _status.value = Realtime.Status.DISCONNECTED
    }

    private fun onMessage(stringMessage: String) {
        val message = supabaseJson.decodeFromString<RealtimeMessage>(stringMessage)
        Realtime.logger.d { "Received message $stringMessage" }
        val channel = subscriptions[message.topic] as? RealtimeChannelImpl
        if(message.ref?.toIntOrNull() == heartbeatRef) {
            Realtime.logger.i { "Heartbeat received" }
            heartbeatRef = 0
        } else {
            Realtime.logger.d { "Received event ${message.event} for channel ${channel?.topic}" }
            channel?.onMessage(message)
        }
    }

    private fun updateJwt(jwt: String) {
        scope.launch {
            subscriptions.values.filter { it.status.value == RealtimeChannel.Status.SUBSCRIBED }.forEach { it.updateAuth(jwt) }
        }
    }

    private suspend fun sendHeartbeat() {
        if (heartbeatRef != 0) {
            heartbeatRef = 0
            ref = 0
            Realtime.logger.e { "Heartbeat timeout. Trying to reconnect in ${config.reconnectDelay}" }
            scope.launch {
                disconnect()
                connect(true)
            }
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

    @SupabaseInternal
    override fun RealtimeChannel.deleteChannel(channel: RealtimeChannel) {
        _subscriptions.remove(channel.topic)
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

    @SupabaseInternal
    override fun RealtimeChannel.addChannel(channel: RealtimeChannel) {
        _subscriptions[channel.topic] = channel
    }

    override suspend fun close() {
        ws?.cancel()
    }

    override suspend fun block() {
        ws?.coroutineContext?.job?.join() ?: error("No connection available")
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
        if(message.event !in listOf("broadcast", "presence", "postgres_changes") || msPerEvent < 0) {
            ws?.sendSerialized(message)
            return
        }
        if(inThrottle) throw RealtimeRateLimitException(config.eventsPerSecond)
        ws?.sendSerialized(message)
        scope.launch {
            inThrottle = true
            delay(msPerEvent.milliseconds)
            inThrottle = false
        }
    }

    fun nextIncrementId(): Int {
        return incrementId++
    }

}
