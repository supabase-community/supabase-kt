package io.github.jan.supabase.realtime.event

import io.github.jan.supabase.logging.e
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.RealtimeChannelImpl
import io.github.jan.supabase.realtime.RealtimeMessage
import kotlin.concurrent.atomics.incrementAndFetch

/**
 * Event that handles an error event
 */
data object RErrorEvent : RealtimeEvent {

    override suspend fun handle(channel: RealtimeChannel, message: RealtimeMessage) {
        channel as RealtimeChannelImpl
        val currentAttempt = channel.joinAttempt.incrementAndFetch()
        if(currentAttempt >= channel.realtime.config.maxAttempts) {
            Realtime.logger.e { "Failed to rejoin channel ${message.topic} after $currentAttempt attempts. Giving up." }
            channel.updateStatus(RealtimeChannel.Status.UNSUBSCRIBED)
            return
        }
        Realtime.logger.e { "Received an error in channel ${message.topic}. That could be as a result of an invalid access token. Trying to rejoin the channel..." }
        channel.scheduleRejoin()
    }

    override fun appliesTo(message: RealtimeMessage): Boolean {
        return message.event == RealtimeChannel.CHANNEL_EVENT_ERROR
    }

}