package io.github.jan.supacompose.realtime.events.actions

import io.github.jan.supacompose.realtime.RealtimeChannel
import kotlinx.serialization.json.JsonObject

/**
 * A channel action is an event received through a [RealtimeChannel]
 */
sealed interface ChannelAction