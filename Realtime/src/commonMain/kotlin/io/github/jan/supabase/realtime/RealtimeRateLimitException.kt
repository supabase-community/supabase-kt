package io.github.jan.supabase.realtime

/**
 * Exception thrown when the rate limit for sending messages to the realtime websocket is exceeded.
 * @param eventsPerSecond the current rate limit. Can be changed within the realtime config.
 */
class RealtimeRateLimitException(eventsPerSecond: Int) : Exception("Rate limit exceeded. Your current limit is set to $eventsPerSecond events per second.")