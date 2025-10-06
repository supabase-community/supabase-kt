package io.github.jan.supabase.auth

import io.github.jan.supabase.OSInformation
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.logging.e
import io.github.jan.supabase.network.NetworkInterceptor
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.http.HttpHeaders
import kotlin.time.Clock

object SessionNetworkInterceptor: NetworkInterceptor.Before {

    override fun call(builder: HttpRequestBuilder, supabase: SupabaseClient) {
        val authHeader = builder.headers[HttpHeaders.Authorization]?.replace("Bearer ", "")
        val currentSession = supabase.auth.currentSessionOrNull()
        val sessionExistsAndExpired = authHeader == currentSession?.accessToken && currentSession != null && currentSession.expiresAt < Clock.System.now()
        val autoRefreshEnabled = supabase.auth.config.alwaysAutoRefresh
        if(sessionExistsAndExpired  && autoRefreshEnabled) {
            val autoRefreshRunning = supabase.auth.isAutoRefreshRunning
            Auth.logger.e { """
                Authenticated request attempted with expired access token. This should not happen. Please report this issue. Trying to refresh session before...
                Auto refresh running: $autoRefreshRunning
                OS: ${OSInformation.CURRENT}
                Session: $currentSession
            """.trimIndent() }
        }
    }

}