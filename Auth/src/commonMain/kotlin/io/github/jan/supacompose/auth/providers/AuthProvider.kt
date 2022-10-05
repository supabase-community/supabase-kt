package io.github.jan.supacompose.auth.providers

import io.github.jan.supacompose.SupabaseClient
import io.github.jan.supacompose.auth.auth
import io.github.jan.supacompose.auth.checkErrors
import io.github.jan.supacompose.auth.generateRedirectUrl
import io.github.jan.supacompose.auth.user.UserSession
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