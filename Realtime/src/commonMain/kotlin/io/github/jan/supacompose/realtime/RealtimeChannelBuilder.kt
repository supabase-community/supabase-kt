package io.github.jan.supacompose.realtime

import io.github.jan.supacompose.realtime.annotiations.ChannelDsl

@ChannelDsl
class RealtimeChannelBuilder @PublishedApi internal constructor(private val topic: String, private val realtimeImpl: RealtimeImpl) {
    private var broadcastJoinConfig = BroadcastJoinConfig(false, false)
    private var presenceJoinConfig = PresenceJoinConfig("")

    fun broadcast(block: BroadcastJoinConfig.() -> Unit) {
        broadcastJoinConfig = BroadcastJoinConfig(acknowledgeBroadcasts = false, receiveOwnBroadcasts = false).apply(block)
    }

    fun presence(block: PresenceJoinConfig.() -> Unit) {
        presenceJoinConfig = PresenceJoinConfig("").apply(block)
    }

    //other presence related stuff

    fun build(): RealtimeChannel {
        return RealtimeChannelImpl(
            realtimeImpl,
            topic,
            broadcastJoinConfig,
            presenceJoinConfig,
            ""
        )
    }

}