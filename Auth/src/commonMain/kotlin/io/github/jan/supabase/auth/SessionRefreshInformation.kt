package io.github.jan.supabase.auth

import io.github.jan.supabase.annotations.SupabaseInternal
import kotlin.time.Instant

@SupabaseInternal
data class SessionRefreshInformation(
    val autoRefreshStartedAt: Instant,
    val lastRefreshedAt: Instant?,
    val refreshingAt: Instant?
)
