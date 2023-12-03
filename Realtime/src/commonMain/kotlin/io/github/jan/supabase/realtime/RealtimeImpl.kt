package io.github.jan.supabase.realtime

import co.touchlab.kermit.Logger
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.buildUrl
import io.github.jan.supabase.collections.AtomicMutableMap
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.exceptions.UnknownRestException
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.SessionStatus
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
import kotlinx.serialization.json.buildJsonObject

internal class RealtimeImpl(override val supabaseClient: SupabaseClient, override val config: Realtime.Config) : Realtime {

    var ws: DefaultClientWebSocketSession? = null
    private val _status = MutableStateFlow(Realtime.Status.DISCONNECTED)
    override val status: StateFlow<Realtime.Status> = _status.asStateFlow()
    private val _subscriptions = AtomicMutableMap<String, RealtimeChannel>()
    override val subscriptions: Map<String, RealtimeChannel>
        get() = _subscriptions.toMap()
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
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

    override suspend fun connect() = connect(false)

    suspend fun connect(reconnect: Boolean) {
        if (reconnect) {
            delay(config.reconnectDelay)
            Logger.d("Realtime") { "Reconnecting..." }
        } else {
            scope.launch {
                supabaseClient.pluginManager.getPluginOrNull(Auth)?.sessionStatus?.collect {
                    if(status.value == Realtime.Status.CONNECTED) {
                        when(it) {
                            is SessionStatus.Authenticated -> updateJwt(it.session.accessToken)
                            is SessionStatus.NotAuthenticated -> {
                                if(config.disconnectOnSessionLoss) {
                                    Logger.w("Realtime") { "No auth session found, disconnecting from realtime websocket"}
                                    disconnect()
                                }
                            }
                            else -> {}
                        }
                    }
                }
            }
        }
        if (status.value == Realtime.Status.CONNECTED) error("Websocket already connected")
        _status.value = Realtime.Status.CONNECTING
        val realtimeUrl = websocketUrl
        try {
            ws = supabaseClient.httpClient.webSocketSession(realtimeUrl)
            _status.value = Realtime.Status.CONNECTED
            Logger.i("Realtime") { "Connected to realtime websocket!" }
            listenForMessages()
            startHeartbeating()
            if(reconnect) {
                rejoinChannels()
            }
        } catch(e: Exception) {
            Logger.e(e, "Realtime") { """
                Error while trying to connect to realtime websocket. Trying again in ${config.reconnectDelay}
                URL: $realtimeUrl
                """.trimIndent() }
            disconnect()
            connect(true)
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
                Logger.e(e, "Realtime") { "Error while listening for messages. Trying again in ${config.reconnectDelay}" }
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
        Logger.d("Realtime") { "Closing websocket connection" }
        messageJob?.cancel()
        ws?.cancel()
        ws = null
        heartbeatJob?.cancel()
        _status.value = Realtime.Status.DISCONNECTED
    }

    private fun onMessage(stringMessage: String) {
        val message = supabaseJson.decodeFromString<RealtimeMessage>(stringMessage)
        Logger.d("Realtime") { "Received message $stringMessage" }
        val channel = subscriptions[message.topic] as? RealtimeChannelImpl
        if(message.ref?.toIntOrNull() == heartbeatRef) {
            Logger.i("Realtime") { "Heartbeat received" }
            heartbeatRef = 0
        } else {
            Logger.d("Realtime") { "Received event ${message.event} for channel ${channel?.topic}" }
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
            Logger.e("Realtime") { "Heartbeat timeout. Trying to reconnect in ${config.reconnectDelay}" }
            scope.launch {
                disconnect()
                connect(true)
            }
            return
        }
        Logger.d("Realtime") { "Sending heartbeat" }
        heartbeatRef = ++ref
        ws?.sendSerialized(RealtimeMessage("phoenix", "heartbeat", buildJsonObject { }, heartbeatRef.toString()))
    }

    override suspend fun removeChannel(channel: RealtimeChannel) {
        if(channel.status.value == RealtimeChannel.Status.SUBSCRIBED) {
            channel.unsubscribe()
        }
        _subscriptions.remove(channel.topic)
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

}
