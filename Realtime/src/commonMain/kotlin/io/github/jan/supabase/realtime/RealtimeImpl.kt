package io.github.jan.supabase.realtime

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.jwt.JWTUtils
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.buildJsonObject
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.fetchAndIncrement
import kotlin.concurrent.atomics.incrementAndFetch
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.time.Clock

@PublishedApi internal class RealtimeImpl(override val supabaseClient: SupabaseClient, override val config: Realtime.Config) : Realtime {

    private val websocketFactory = config.websocketFactory ?: KtorRealtimeWebsocketFactory(supabaseClient.httpClient.httpClient)
    private var ws: RealtimeWebsocket? = null
    @Suppress("MagicNumber")
    private val _status = MutableStateFlow(Realtime.Status.DISCONNECTED)
    override val status: StateFlow<Realtime.Status> = _status.asStateFlow()
    private val _subscriptions = AtomicMutableMap<String, RealtimeChannel>()
    override val subscriptions: Map<String, RealtimeChannel> = _subscriptions
    private val scope = CoroutineScope(supabaseClient.coroutineDispatcher + SupervisorJob())
    private val mutex = Mutex()
    private val _accessToken = AtomicReference<String?>(null)
    val accessToken get() = _accessToken.load()
    private var heartbeatJob: Job? = null
    private var messageJob: Job? = null
    internal val ref = AtomicInt(0)
    private val heartbeatRef = AtomicInt(0)
    override val apiVersion: Int
        get() = Realtime.API_VERSION

    override val pluginKey: String
        get() = Realtime.key

    override var serializer = config.serializer ?: supabaseClient.defaultSerializer
    private val websocketUrl = realtimeWebsocketUrl()
    private val incrementId = AtomicInt(0)

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
            currentCoroutineContext().ensureActive()
            Realtime.logger.e(e) { """
                Error while trying to connect to realtime websocket. Trying again in ${config.reconnectDelay}
                URL: $websocketUrl
                """.trimIndent() }
            reconnect()
        }
    }

    override fun addChannel(channel: RealtimeChannel) {
        _subscriptions[channel.topic] = channel
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
                currentCoroutineContext().ensureActive()
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
        val ref = message.ref?.toIntOrNull()
        if(ref != null && heartbeatRef.compareAndSet(ref, 0)) {
            Realtime.logger.i { "Heartbeat received" }
        } else {
            Realtime.logger.d { "Received event ${message.event} for channel ${channel?.topic}" }
            channel?.onMessage(message)
        }
    }

    @OptIn(ExperimentalEncodingApi::class)
    override suspend fun setAuth(token: String?) {
        val newToken = token ?: config.accessToken(supabaseClient)

        if(newToken != null) {
            val exp = JWTUtils.decodeJwt(newToken).claimsResponse.claims.exp ?: error("exp claim missing")
            val now = Clock.System.now()
            val diff = exp - now
            if(diff.isNegative()) {
                Realtime.logger.w { "Token is expired. Not sending it to realtime." }
                return
            }
        }
        this._accessToken.store(newToken)
        scope.launch {
            subscriptions.values.filter { it.status.value == RealtimeChannel.Status.SUBSCRIBED }.forEach { it.updateAuth(accessToken) }
        }
    }

    private suspend fun sendHeartbeat() {
        if (heartbeatRef.load() != 0) {
            heartbeatRef.store(0)
            ref.store(0)
            Realtime.logger.e { "Heartbeat timeout. Trying to reconnect in ${config.reconnectDelay}" }
            reconnect()
            return
        }
        Realtime.logger.d { "Sending heartbeat" }
        heartbeatRef.store(ref.incrementAndFetch())
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
            currentCoroutineContext().ensureActive()
            Realtime.logger.e(e) { "Error while sending message $message. Reconnecting in ${config.reconnectDelay}" }
            reconnect()
        }
    }

    fun nextIncrementId(): Int {
        return incrementId.fetchAndIncrement()
    }

    private fun reconnect() {
        scope.launch {
            disconnect()
            connect(true)
        }
    }

}
