package io.github.jan.supacompose.realtime.events

/**
 * An event listener for [ChannelAction]s
 */
fun interface EventListener {

    /**
     * Called whenever a [ChannelAction] is received
     */
    fun onEvent(event: ChannelAction)

}