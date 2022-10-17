package io.github.jan.supabase.auth.providers

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.checkErrors
import io.github.jan.supabase.auth.generateRedirectUrl
import io.github.jan.supabase.auth.user.UserSession
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kotlinx.serialization.json.JsonObject

interface AuthProvider<C, R> {

    suspend fun login(
        supabaseClient: SupabaseClient,
        onSuccess: suspend (UserSession) -> Unit,
        redirectUrl: String? = null,
        config: (C.() -> Unit)? = null
    )

    suspend fun signUp(
        supabaseClient: SupabaseClient,
        onSuccess: suspend (UserSession) -> Unit,
        redirectUrl: String? = null,
        config: (C.() -> Unit)? = null
    ): R

}