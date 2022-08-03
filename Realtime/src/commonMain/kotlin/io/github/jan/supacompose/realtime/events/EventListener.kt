package io.github.jan.supacompose.realtime.events

fun interface EventListener {

    fun onEvent(event: ChannelAction)

}