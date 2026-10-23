package io.github.jan.supabase.realtime.event

import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.RealtimeMessage

/**
 * Interface for handling realtime events.
 */
internal sealed interface RealtimeEvent {

    /**
     * Handles the event.
     * @param channel The channel the event was received on.
     * @param message The message that was received.
     */
    suspend fun handle(channel: RealtimeChannel, message: RealtimeMessage)

    /**
     * Checks if the event applies to the message.
     */
    fun appliesTo(message: RealtimeMessage): Boolean

    companion object {

        internal val EVENTS = setOf( // Kotlin doesn't provide a way to get all objects of a sealed interface outside the JVM, so we have to list them manually
            RBroadcastEvent,
            RCloseEvent,
            RErrorEvent,
            RPostgresChangesEvent,
            RPostgresServerChangesEvent,
            RPresenceStateEvent,
            RPresenceDiffEvent,
            RSystemEvent,
            RTokenExpiredEvent,
            RSystemReplyEvent
        )

        /**
         * Resolves the event from a realtime message: the one event that applies to it, or null if none does.
         * The events' conditions don't overlap, so their order doesn't matter.
         */
        fun resolveEvent(realtimeMessage: RealtimeMessage): RealtimeEvent? {
            return EVENTS.singleOrNull { it.appliesTo(realtimeMessage) }
        }
        
    }
    
}