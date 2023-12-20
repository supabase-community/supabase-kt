package io.github.jan.supabase.realtime

import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.realtime.annotations.ChannelDsl

/**
 * Used to build a realtime channel
 */
@ChannelDsl
class RealtimeChannelBuilder @PublishedApi internal constructor(private val topic: String, private val realtimeImpl: RealtimeImpl) {

    private var broadcastJoinConfig = BroadcastJoinConfig(acknowledgeBroadcasts = false, receiveOwnBroadcasts = false)
    private var presenceJoinConfig = PresenceJoinConfig("")

    /**
     * Sets the broadcast join config
     */
    fun broadcast(block: BroadcastJoinConfig.() -> Unit) {
        broadcastJoinConfig = BroadcastJoinConfig(acknowledgeBroadcasts = false, receiveOwnBroadcasts = false).apply(block)
    }

    /**
     * Sets the presence join config
     * @param block The presence join config
     */
    fun presence(block: PresenceJoinConfig.() -> Unit) {
        presenceJoinConfig = PresenceJoinConfig("").apply(block)
    }

    @SupabaseInternal
    fun build(): RealtimeChannel {
        return RealtimeChannelImpl(
            realtimeImpl,
            topic,
            broadcastJoinConfig,
            presenceJoinConfig
        )
    }

}