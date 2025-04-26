package io.github.jan.supabase.auth

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.user.UserSession

internal fun invalidArg(message: String): Nothing = throw IllegalArgumentException(message)

internal expect suspend fun SupabaseClient.openExternalUrl(url: String)

internal expect suspend fun Auth.startExternalAuth(
    redirectUrl: String?,
    getUrl: suspend (redirectTo: String?) -> String,
    onSessionSuccess: suspend (UserSession) -> Unit
)