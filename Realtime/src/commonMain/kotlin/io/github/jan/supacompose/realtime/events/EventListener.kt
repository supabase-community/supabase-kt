package io.github.jan.supacompose.realtime.events

import io.github.jan.supacompose.realtime.events.actions.ChannelAction

/**
 * An event listener for [ChannelAction]s
 */
fun interface EventListener {

    /**
     * Called whenever a [ChannelAction] is received
     */
    fun onEvent(event: ChannelAction)

}