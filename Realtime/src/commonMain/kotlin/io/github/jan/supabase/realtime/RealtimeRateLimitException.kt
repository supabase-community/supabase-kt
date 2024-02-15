package io.github.jan.supabase.realtime

class RealtimeRateLimitException(eventsPerSecond: Int) : Exception("Rate limit exceeded. Your current limit is set to $eventsPerSecond events per second.")