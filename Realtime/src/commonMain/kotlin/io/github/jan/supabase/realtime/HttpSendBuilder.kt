package io.github.jan.supabase.realtime

import kotlin.time.Duration

/**
 * Builder for [RealtimeChannel.httpSend]
 */
class HttpSendBuilder {

    /**
     * Duration after which the request times out.
     */
    var timeout: Duration? = null

}